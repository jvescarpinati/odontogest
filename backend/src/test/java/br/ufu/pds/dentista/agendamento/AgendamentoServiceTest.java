package br.ufu.pds.dentista.agendamento;

import br.ufu.pds.dentista.common.ConflitoException;
import br.ufu.pds.dentista.common.NaoEncontradoException;
import br.ufu.pds.dentista.paciente.Paciente;
import br.ufu.pds.dentista.paciente.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do AgendamentoService (SCRUM-36).
 * Cobre: criação sem etapaId, choque de horário (total, parcial, bordas),
 * concorrência (excludeId no PUT), cancelamento, e validações de negócio.
 * Não usa banco — o repositório é um mock (Mockito).
 */
@DisplayName("AgendamentoService — regras de negócio e anti-conflito")
@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock AgendamentoRepository agendamentoRepository;
    @Mock PacienteRepository    pacienteRepository;
    @InjectMocks AgendamentoService service;

    // Datas fixas no futuro para todos os testes
    private static final LocalDateTime BASE   = LocalDateTime.now().plusDays(1)
                                                   .withHour(10).withMinute(0).withSecond(0).withNano(0);
    private static final LocalDateTime INICIO = BASE;                     // 10:00
    private static final LocalDateTime FIM    = BASE.plusHours(1);       // 11:00

    private Paciente pacienteAtivo;

    @BeforeEach
    void setUp() {
        pacienteAtivo = new Paciente("Maria Silva", "52998224725",
                LocalDate.of(1990, 5, 20), "34999990000", "maria@email.com", "Rua A");
        pacienteAtivo.setId(1L);
    }

    // ── Criação com etapaId null (US04 desacoplada da US03) ──────────────────

    @Test
    @DisplayName("criar sem etapaId salva o agendamento e retorna etapaId null")
    void criarSemEtapaIdRetornaEtapaIdNull() {
        when(pacienteRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pacienteAtivo));
        when(agendamentoRepository.contarConflitos(any(), any(), eq(0L), eq(StatusAgendamento.CANCELADA)))
                .thenReturn(0L);
        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AgendamentoResponse resp = service.criar(req(1L, INICIO, FIM));

        assertThat(resp.etapaId()).isNull();
        assertThat(resp.status()).isEqualTo("AGENDADA");
        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    // ── Choque de horário — sobreposição total ────────────────────────────────

    @Test
    @DisplayName("criar com horário idêntico ao de outro agendamento ativo lança ConflitoException")
    void criarComHorarioIguaLancaConflito() {
        when(pacienteRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pacienteAtivo));
        when(agendamentoRepository.contarConflitos(INICIO, FIM, 0L, StatusAgendamento.CANCELADA))
                .thenReturn(1L);

        assertThatThrownBy(() -> service.criar(req(1L, INICIO, FIM)))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("agendamento ativo");

        verify(agendamentoRepository, never()).save(any());
    }

    // ── Choque de horário — sobreposição parcial ──────────────────────────────

    @Test
    @DisplayName("criar com horário que começa dentro de outro agendamento lança ConflitoException")
    void criarComSobreposicaoParcialLancaConflito() {
        // Novo agendamento: 10:30 – 11:30 (dentro de um existente 10:00 – 11:00)
        LocalDateTime novoInicio = INICIO.plusMinutes(30);
        LocalDateTime novoFim    = FIM.plusMinutes(30);

        when(pacienteRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pacienteAtivo));
        when(agendamentoRepository.contarConflitos(novoInicio, novoFim, 0L, StatusAgendamento.CANCELADA))
                .thenReturn(1L);

        assertThatThrownBy(() -> service.criar(req(1L, novoInicio, novoFim)))
                .isInstanceOf(ConflitoException.class);

        verify(agendamentoRepository, never()).save(any());
    }

    // ── Bordas exatas — NÃO devem conflitar ──────────────────────────────────

    @Test
    @DisplayName("criar com início igual ao fim de outro agendamento (borda) não conflita")
    void criarComBordaExataPermitido() {
        // Novo agendamento começa exatamente quando o anterior termina: 11:00 – 12:00
        LocalDateTime bordaInicio = FIM;
        LocalDateTime bordaFim    = FIM.plusHours(1);

        when(pacienteRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pacienteAtivo));
        // A query no banco retorna 0 (sem conflito) porque fim1 == inicio2 → fim1 > inicio2 é falso
        when(agendamentoRepository.contarConflitos(bordaInicio, bordaFim, 0L, StatusAgendamento.CANCELADA))
                .thenReturn(0L);
        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AgendamentoResponse resp = service.criar(req(1L, bordaInicio, bordaFim));

        assertThat(resp).isNotNull();
        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    // ── Concorrência: PUT exclui o próprio id do check ───────────────────────

    @Test
    @DisplayName("atualizar mantendo o próprio horário não conflita consigo mesmo (excludeId correto)")
    void atualizarMantendoProprioHorarioPermitido() {
        Agendamento existente = new Agendamento(pacienteAtivo, INICIO, FIM, null);
        existente.setId(5L);

        when(agendamentoRepository.findById(5L)).thenReturn(Optional.of(existente));
        // Chama contarConflitos com excludeId = 5L (o próprio) → retorna 0
        when(agendamentoRepository.contarConflitos(INICIO, FIM, 5L, StatusAgendamento.CANCELADA))
                .thenReturn(0L);

        AgendamentoResponse resp = service.atualizar(5L, req(1L, INICIO, FIM));

        assertThat(resp.id()).isEqualTo(5L);
        verify(agendamentoRepository).contarConflitos(INICIO, FIM, 5L, StatusAgendamento.CANCELADA);
    }

    @Test
    @DisplayName("atualizar agendamento cujo horário conflita com outro lança ConflitoException")
    void atualizarComChoqueDeOutroLancaConflito() {
        Agendamento existente = new Agendamento(pacienteAtivo, INICIO, FIM, null);
        existente.setId(5L);

        LocalDateTime novoInicio = INICIO.plusMinutes(30);
        LocalDateTime novoFim    = FIM.plusMinutes(30);

        when(agendamentoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(agendamentoRepository.contarConflitos(novoInicio, novoFim, 5L, StatusAgendamento.CANCELADA))
                .thenReturn(1L); // outro agendamento conflita

        assertThatThrownBy(() -> service.atualizar(5L, req(1L, novoInicio, novoFim)))
                .isInstanceOf(ConflitoException.class);
    }

    // ── Não pode alterar agendamento cancelado ────────────────────────────────

    @Test
    @DisplayName("atualizar agendamento cancelado lança ConflitoException")
    void atualizarCanceladoLancaConflito() {
        Agendamento cancelado = new Agendamento(pacienteAtivo, INICIO, FIM, null);
        cancelado.setId(3L);
        cancelado.cancelar();

        when(agendamentoRepository.findById(3L)).thenReturn(Optional.of(cancelado));

        assertThatThrownBy(() -> service.atualizar(3L, req(1L, INICIO.plusDays(1), FIM.plusDays(1))))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("cancelado");

        verify(agendamentoRepository, never()).save(any());
    }

    // ── Cancelar duas vezes ───────────────────────────────────────────────────

    @Test
    @DisplayName("cancelar agendamento já cancelado lança ConflitoException")
    void cancelarJaCanceladoLancaConflito() {
        Agendamento cancelado = new Agendamento(pacienteAtivo, INICIO, FIM, null);
        cancelado.setId(2L);
        cancelado.cancelar();

        when(agendamentoRepository.findById(2L)).thenReturn(Optional.of(cancelado));

        assertThatThrownBy(() -> service.cancelar(2L))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("cancelado");
    }

    // ── Paciente inexistente ──────────────────────────────────────────────────

    @Test
    @DisplayName("criar com paciente inexistente lança NaoEncontradoException")
    void criarComPacienteInexistenteLancaNaoEncontrado() {
        when(pacienteRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(req(99L, INICIO, FIM)))
                .isInstanceOf(NaoEncontradoException.class)
                .hasMessageContaining("Paciente");

        verify(agendamentoRepository, never()).save(any());
    }

    // ── Agendamento inexistente ───────────────────────────────────────────────

    @Test
    @DisplayName("buscar agendamento inexistente lança NaoEncontradoException")
    void buscarInexistenteLancaNaoEncontrado() {
        when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(NaoEncontradoException.class)
                .hasMessageContaining("Agendamento");
    }

    // ── Validação de datas ────────────────────────────────────────────────────

    @Test
    @DisplayName("criar com fim igual ao início lança IllegalArgumentException")
    void criarComFimIgualInicioLancaErro() {
        assertThatThrownBy(() -> service.criar(req(1L, INICIO, INICIO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    @DisplayName("criar com fim antes do início lança IllegalArgumentException")
    void criarComFimAntesInicioLancaErro() {
        assertThatThrownBy(() -> service.criar(req(1L, INICIO, INICIO.minusMinutes(30))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── Filtro de visão diária / semanal ─────────────────────────────────────

    @Test
    @DisplayName("listarPorIntervalo com fim <= início lança IllegalArgumentException")
    void listarPorIntervaloInvalidoLancaErro() {
        assertThatThrownBy(() -> service.listarPorIntervalo(INICIO, INICIO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Intervalo");
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private static AgendamentoRequest req(Long pacienteId, LocalDateTime inicio, LocalDateTime fim) {
        return new AgendamentoRequest(pacienteId, inicio, fim, null);
    }
}
