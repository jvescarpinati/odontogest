package br.ufu.pds.dentista.anamnese;

import br.ufu.pds.dentista.paciente.Paciente;
import br.ufu.pds.dentista.paciente.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
class AnamneseIntegrationTest {

    @Autowired
    AnamneseService service;

    @Autowired
    PacienteRepository pacienteRepository;

    @Test
    void salvarEConsultarAnamnese() {
        Paciente p = pacienteRepository.save(new Paciente(
                "Carlos", "52998224725", LocalDate.of(1990, 1, 1),
                "34999999999", "carlos@email.com", "Rua A"));

        AnamneseDTO dto = new AnamneseDTO(
                null, p.getId(), null,
                "Penicilina", "Diabetes", "Insulina",
                "Cuidado com anestesia", null);

        AnamneseDTO salvo = service.salvar(dto);

        assertThat(salvo.id()).isNotNull();
        assertThat(salvo.alergias()).isEqualTo("Penicilina");
        assertThat(salvo.dataModificacao()).isNotNull();

        AnamneseDTO buscado = service.buscarPorPaciente(p.getId());
        assertThat(buscado.pacienteNome()).isEqualTo("Carlos");
    }
}
