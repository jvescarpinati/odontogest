package br.ufu.pds.dentista.agendamento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração do AgendamentoController (SCRUM-36).
 * Usa H2 em memória (banco criado pelo Hibernate com ddl-auto=create-drop),
 * portanto nenhum PostgreSQL local é necessário.
 *
 * Cobre: criação, choque de horário (sobreposição total e parcial), bordas,
 * listagem por intervalo (visão diária / semanal) e mudança de status.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:test_agendamento;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("AgendamentoController — integração com H2")
class AgendamentoControllerTest {

    @Autowired MockMvc mvc;

    // Datas no futuro remoto para nunca conflitar com @Future na request
    private static final String INICIO_1 = "2099-06-01T10:00:00";
    private static final String FIM_1    = "2099-06-01T11:00:00";
    private static final String INICIO_2 = "2099-06-02T14:00:00";
    private static final String FIM_2    = "2099-06-02T15:00:00";

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Cria um paciente e retorna seu id (long) via MockMvc. */
    private long criarPaciente() throws Exception {
        String body = mvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria Silva","cpf":"529.982.247-25",
                                 "dataNascimento":"1990-05-20","telefone":"(34) 99999-0000",
                                 "email":"a@b.com","endereco":"Rua X, 10"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // extrai o id do JSON retornado (simples, sem lib extra)
        String idStr = body.replaceAll(".*\"id\":(\\d+).*", "$1");
        return Long.parseLong(idStr);
    }

    private String jsonAg(long pacienteId, String inicio, String fim) {
        return """
               {"pacienteId":%d,"dataHoraInicio":"%s","dataHoraFim":"%s","observacoes":"Consulta de rotina"}
               """.formatted(pacienteId, inicio, fim);
    }

    // ── POST — criação válida ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST válido cria agendamento com status AGENDADA e etapaId null")
    void criarAgendamentoValido() throws Exception {
        long pid = criarPaciente();

        mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, INICIO_1, FIM_1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.etapaId").doesNotExist())
                .andExpect(jsonPath("$.pacienteNome").value("Maria Silva"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    // ── POST — paciente inexistente ───────────────────────────────────────────

    @Test
    @DisplayName("POST com paciente inexistente retorna 404")
    void criarComPacienteInexistente() throws Exception {
        mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(9999L, INICIO_1, FIM_1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Paciente não encontrado"));
    }

    // ── POST — validação de campos obrigatórios ───────────────────────────────

    @Test
    @DisplayName("POST sem campos obrigatórios retorna 400 com erros por campo")
    void criarSemCamposObrigatorios() throws Exception {
        mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.pacienteId").value("Paciente é obrigatório"))
                .andExpect(jsonPath("$.campos.dataHoraInicio").value("Data/hora de início é obrigatória"))
                .andExpect(jsonPath("$.campos.dataHoraFim").value("Data/hora de fim é obrigatória"));
    }

    // ── POST — choque de horário total ────────────────────────────────────────

    @Test
    @DisplayName("POST com horário idêntico ao de agendamento ativo retorna 409")
    void criarComChoqueTotal() throws Exception {
        long pid = criarPaciente();
        // primeiro agendamento: OK
        mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, INICIO_1, FIM_1)))
                .andExpect(status().isCreated());

        // segundo com mesmo horário: conflito
        mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, INICIO_1, FIM_1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe um agendamento ativo neste horário. Verifique a agenda e escolha outro horário."));
    }

    // ── POST — choque de horário parcial ──────────────────────────────────────

    @Test
    @DisplayName("POST com início dentro de agendamento existente (sobreposição parcial) retorna 409")
    void criarComSobreposicaoParcial() throws Exception {
        long pid = criarPaciente();
        // existente: 10:00 – 11:00
        mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, INICIO_1, FIM_1)))
                .andExpect(status().isCreated());

        // novo:      10:30 – 11:30  → parcial à direita
        mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, "2099-06-01T10:30:00", "2099-06-01T11:30:00")))
                .andExpect(status().isConflict());
    }

    // ── POST — bordas exatas (fim1 == início2): NÃO deve conflitar ────────────

    @Test
    @DisplayName("POST com início igual ao fim de agendamento existente (borda) é permitido")
    void criarNaBordaExataPermitido() throws Exception {
        long pid = criarPaciente();
        // existente: 10:00 – 11:00
        mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, INICIO_1, FIM_1)))
                .andExpect(status().isCreated());

        // novo começa exatamente quando o anterior termina: 11:00 – 12:00
        mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, "2099-06-01T11:00:00", "2099-06-01T12:00:00")))
                .andExpect(status().isCreated());
    }

    // ── POST — cancelado libera o horário ─────────────────────────────────────

    @Test
    @DisplayName("agendamento cancelado libera o horário para novo agendamento")
    void canceladoLiberaHorario() throws Exception {
        long pid = criarPaciente();

        // cria e obtém o id
        String respCriacao = mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, INICIO_1, FIM_1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long agId = Long.parseLong(respCriacao.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // cancela
        mvc.perform(patch("/api/agendamentos/" + agId + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        // mesmo horário agora deve ser aceito
        mvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, INICIO_1, FIM_1)))
                .andExpect(status().isCreated());
    }

    // ── GET por intervalo — visão diária e semanal ────────────────────────────

    @Test
    @DisplayName("GET por intervalo retorna apenas os agendamentos dentro do período")
    void listarPorIntervalo() throws Exception {
        long pid = criarPaciente();

        mvc.perform(post("/api/agendamentos").contentType(MediaType.APPLICATION_JSON)
                .content(jsonAg(pid, INICIO_1, FIM_1))).andExpect(status().isCreated());
        mvc.perform(post("/api/agendamentos").contentType(MediaType.APPLICATION_JSON)
                .content(jsonAg(pid, INICIO_2, FIM_2))).andExpect(status().isCreated());

        // filtra apenas o dia 1 (visão diária)
        mvc.perform(get("/api/agendamentos")
                        .param("inicio", "2099-06-01T00:00:00")
                        .param("fim",    "2099-06-02T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].dataHoraInicio").value("2099-06-01T10:00:00"));

        // filtra a semana inteira (visão semanal — encontra os dois)
        mvc.perform(get("/api/agendamentos")
                        .param("inicio", "2099-06-01T00:00:00")
                        .param("fim",    "2099-06-08T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── PATCH confirmar ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /confirmar atualiza status para CONFIRMADA")
    void confirmarAgendamento() throws Exception {
        long pid = criarPaciente();

        String resp = mvc.perform(post("/api/agendamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, INICIO_1, FIM_1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long agId = Long.parseLong(resp.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(patch("/api/agendamentos/" + agId + "/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    // ── PUT — edição sem conflitar consigo mesmo (concorrência) ──────────────

    @Test
    @DisplayName("PUT mantendo o mesmo horário do próprio agendamento é permitido (excludeId)")
    void atualizarMantendoHorarioPermitido() throws Exception {
        long pid = criarPaciente();

        String resp = mvc.perform(post("/api/agendamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAg(pid, INICIO_1, FIM_1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long agId = Long.parseLong(resp.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // PUT com o mesmo horário — não deve conflitar com si mesmo
        mvc.perform(put("/api/agendamentos/" + agId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":%d,"dataHoraInicio":"%s","dataHoraFim":"%s",
                                 "observacoes":"Atualizado"}
                                """.formatted(pid, INICIO_1, FIM_1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observacoes").value("Atualizado"));
    }

    // ── GET por paciente ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /paciente/{id} retorna todos os agendamentos do paciente em ordem")
    void listarPorPaciente() throws Exception {
        long pid = criarPaciente();

        mvc.perform(post("/api/agendamentos").contentType(MediaType.APPLICATION_JSON)
                .content(jsonAg(pid, INICIO_2, FIM_2))).andExpect(status().isCreated());
        mvc.perform(post("/api/agendamentos").contentType(MediaType.APPLICATION_JSON)
                .content(jsonAg(pid, INICIO_1, FIM_1))).andExpect(status().isCreated());

        // deve retornar 2, em ordem cronológica (INICIO_1 < INICIO_2)
        mvc.perform(get("/api/agendamentos/paciente/" + pid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].dataHoraInicio").value("2099-06-01T10:00:00"))
                .andExpect(jsonPath("$[1].dataHoraInicio").value("2099-06-02T14:00:00"));
    }
}
