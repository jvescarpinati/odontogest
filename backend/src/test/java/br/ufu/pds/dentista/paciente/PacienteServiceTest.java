package br.ufu.pds.dentista.paciente;

import br.ufu.pds.dentista.common.ConflitoException;
import br.ufu.pds.dentista.common.NaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Regras de negócio do cadastro: CPF único (no cadastro e na edição) e paciente existente.
 * Teste unitário: o repositório é um "mock" (dublê), então nenhum banco é usado.
 */
@DisplayName("PacienteService - regras de unicidade e existência")
@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    PacienteRepository repository;

    @InjectMocks
    PacienteService service;

    private static PacienteRequest req(String nome, String cpf) {
        return new PacienteRequest(nome, cpf, LocalDate.of(1990, 5, 20),
                "(34) 99999-0000", "  Maria@Email.COM ", "Rua X, 10");
    }

    private static Paciente pacienteExistente() {
        return new Paciente("Maria Silva", "52998224725", LocalDate.of(1990, 5, 20),
                "(34) 99999-0000", "maria@email.com", "Rua X, 10");
    }

    // ---------- cadastro ----------

    @Test
    void cadastroComCpfJaExistenteELancaConflito() {
        when(repository.existsByCpf("52998224725")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(req("Outra Pessoa", "529.982.247-25")))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("CPF");

        verify(repository, never()).save(any());
    }

    @Test
    void cadastroVerificaDuplicidadeComCpfSemMascara() {
        // "529.982.247-25" e "52998224725" são o mesmo CPF: a checagem usa a forma normalizada
        when(repository.existsByCpf("52998224725")).thenReturn(false);
        when(repository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        service.criar(req("Maria Silva", "529.982.247-25"));

        verify(repository).existsByCpf("52998224725");
    }

    @Test
    void cadastroValidoSalvaComDadosNormalizados() {
        when(repository.existsByCpf("52998224725")).thenReturn(false);
        when(repository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteResponse resp = service.criar(req("  Maria Silva  ", "529.982.247-25"));

        assertThat(resp.nome()).isEqualTo("Maria Silva");
        assertThat(resp.cpf()).isEqualTo("52998224725");
        assertThat(resp.email()).isEqualTo("maria@email.com");
        verify(repository).save(any(Paciente.class));
    }

    // ---------- edição ----------

    @Test
    void edicaoParaCpfDeOutroPacienteLancaConflitoENaoAlteraDados() {
        Paciente existente = pacienteExistente();
        when(repository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByCpfAndIdNot("11144477735", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, req("Maria Silva", "111.444.777-35")))
                .isInstanceOf(ConflitoException.class);

        assertThat(existente.getCpf()).isEqualTo("52998224725"); // continua igual
    }

    @Test
    void edicaoMantendoOProprioCpfEPermitida() {
        Paciente existente = pacienteExistente();
        when(repository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByCpfAndIdNot("52998224725", 1L)).thenReturn(false);

        PacienteResponse resp = service.atualizar(1L, req("Maria Silva Oliveira", "529.982.247-25"));

        assertThat(resp.nome()).isEqualTo("Maria Silva Oliveira");
        assertThat(existente.getNome()).isEqualTo("Maria Silva Oliveira");
    }

    @Test
    void edicaoDePacienteInexistenteLancaNaoEncontrado() {
        when(repository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99L, req("Maria", "52998224725")))
                .isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void buscaDePacienteInexistenteLancaNaoEncontrado() {
        when(repository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(NaoEncontradoException.class);
    }
}
