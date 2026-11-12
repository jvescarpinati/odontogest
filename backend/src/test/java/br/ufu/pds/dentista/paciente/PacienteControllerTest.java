package br.ufu.pds.dentista.paciente;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PacienteControllerTest {

    @Autowired MockMvc mvc;

    static String json(String nome, String cpf) {
        return """
            {"nome":"%s","cpf":"%s","dataNascimento":"1990-05-20",
             "telefone":"(34) 99999-0000","email":"a@b.com","endereco":"Rua X, 10"}
            """.formatted(nome, cpf);
    }

    @Test
    void cpfUtils() {
        assertThat(CpfUtils.isValido("529.982.247-25")).isTrue();
        assertThat(CpfUtils.isValido("529.982.247-24")).isFalse();
        assertThat(CpfUtils.isValido("111.111.111-11")).isFalse();
        assertThat(CpfUtils.isValido("123")).isFalse();
    }

    @Test
    void fluxoCompleto() throws Exception {
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                .content(json("Maria Silva", "529.982.247-25")))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.cpf").value("52998224725"))
           .andExpect(jsonPath("$.id").value(1));

        // duplicado, mesmo com máscara diferente
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                .content(json("Outra", "52998224725")))
           .andExpect(status().isConflict());

        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                .content(json("João Souza", "111.444.777-35")))
           .andExpect(status().isCreated());

        mvc.perform(get("/api/pacientes").param("busca", "mar"))
           .andExpect(jsonPath("$.length()").value(1))
           .andExpect(jsonPath("$[0].nome").value("Maria Silva"));
        mvc.perform(get("/api/pacientes").param("busca", "111.444"))
           .andExpect(jsonPath("$[0].nome").value("João Souza"));

        // editar mantendo o próprio CPF: ok
        mvc.perform(put("/api/pacientes/1").contentType(MediaType.APPLICATION_JSON)
                .content(json("Maria S. Oliveira", "52998224725")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.nome").value("Maria S. Oliveira"));
        // editar para o CPF de outro paciente: 409
        mvc.perform(put("/api/pacientes/1").contentType(MediaType.APPLICATION_JSON)
                .content(json("Maria", "11144477735")))
           .andExpect(status().isConflict());

        mvc.perform(delete("/api/pacientes/2")).andExpect(status().isNoContent());
        mvc.perform(get("/api/pacientes/2")).andExpect(status().isNotFound());
        mvc.perform(get("/api/pacientes")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void validacoes() throws Exception {
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cpf\":\"123.456.789-00\",\"dataNascimento\":\"2999-01-01\",\"email\":\"x\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.campos.nome").value("Nome é obrigatório"))
           .andExpect(jsonPath("$.campos.cpf").value("CPF inválido"))
           .andExpect(jsonPath("$.campos.dataNascimento").exists())
           .andExpect(jsonPath("$.campos.telefone").value("Telefone é obrigatório"))
           .andExpect(jsonPath("$.campos.email").value("E-mail inválido"));
    }
}
