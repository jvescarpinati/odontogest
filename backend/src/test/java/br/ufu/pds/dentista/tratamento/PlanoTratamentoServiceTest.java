
package br.ufu.pds.dentista.tratamento;

import br.ufu.pds.dentista.agendamento.AgendamentoRepository;
import br.ufu.pds.dentista.paciente.Paciente;
import br.ufu.pds.dentista.paciente.PacienteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanoTratamentoServiceTest {

    @Mock
    private PlanoTratamentoRepository planoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private PlanoTratamentoService service;

    private Paciente paciente;

    @BeforeEach
    void preparar() {
        paciente = mock(Paciente.class);
    }

    // TESTES DE CRIAÇÃO

    @Test
    void naoDeveCriarPlanoSemTitulo() {

        assertThrows(
            IllegalArgumentException.class,
            () -> service.criar("   ", 1L)
        );

        verifyNoInteractions(planoRepository);
    }

    @Test
    void naoDeveCriarPlanoParaPacienteInexistente() {

        when(pacienteRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(
            NoSuchElementException.class,
            () -> service.criar("Tratamento", 99L)
        );

        verify(planoRepository, never())
            .save(any(PlanoTratamento.class));
    }

    @Test
    void deveCriarPlanoParaPacienteExistente() {

        when(pacienteRepository.findById(1L))
            .thenReturn(Optional.of(paciente));

        when(planoRepository.save(any(PlanoTratamento.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        PlanoTratamento plano =
            service.criar("Tratamento ortodontico", 1L);

        assertEquals(
            "Tratamento ortodontico",
            plano.getTitulo()
        );

        assertEquals(
            StatusPlanoTratamento.EM_ELABORACAO,
            plano.getStatus()
        );

        verify(planoRepository)
            .save(any(PlanoTratamento.class));
    }

    // TESTES DE ETAPAS

    @Test
    void deveAdicionarEtapaAoPlanoEmElaboracao() {

        PlanoTratamento plano =
            new PlanoTratamento("Tratamento", paciente);

        when(planoRepository.findById(1L))
            .thenReturn(Optional.of(plano));

        when(planoRepository.save(plano))
            .thenReturn(plano);

        service.adicionarEtapa(1L, "Avaliacao inicial");

        assertEquals(1, plano.getEtapas().size());

        assertEquals(
            "Avaliacao inicial",
            plano.getEtapas().get(0).getProcedimento()
        );

        assertEquals(
            1,
            plano.getEtapas().get(0).getOrdem()
        );

        verify(planoRepository).save(plano);
    }

    @Test
    void naoDeveAdicionarEtapaAoPlanoAtivo() {

        PlanoTratamento plano =
            new PlanoTratamento("Tratamento", paciente);

        plano.alterarStatus(StatusPlanoTratamento.ATIVO);

        when(planoRepository.findById(1L))
            .thenReturn(Optional.of(plano));

        assertThrows(
            IllegalStateException.class,
            () -> service.adicionarEtapa(1L, "Nova etapa")
        );

        verify(planoRepository, never())
            .save(any(PlanoTratamento.class));
    }

    @Test
    void naoDeveRemoverEtapaVinculadaAAgendamento() {

        PlanoTratamento plano =
            new PlanoTratamento("Tratamento", paciente);

        plano.adicionarEtapa("Avaliacao inicial");

        EtapaTratamento etapa = plano.getEtapas().get(0);

        ReflectionTestUtils.setField(etapa, "id", 10L);

        when(planoRepository.findById(1L))
            .thenReturn(Optional.of(plano));

        when(agendamentoRepository.existsByEtapaId(10L))
            .thenReturn(true);

        IllegalStateException erro = assertThrows(
            IllegalStateException.class,
            () -> service.removerEtapa(1L, 10L)
        );

        assertEquals(
            "Não é possível remover uma etapa vinculada a um agendamento.",
            erro.getMessage()
        );

        assertEquals(1, plano.getEtapas().size());

        verify(planoRepository, never())
            .save(any(PlanoTratamento.class));
    }

    // TESTES DE REORDENAÇÃO

    @Test
    void deveReordenarEtapasDoPlanoEmElaboracao() {

        PlanoTratamento plano =
            new PlanoTratamento("Tratamento", paciente);

        plano.adicionarEtapa("Primeira etapa");
        plano.adicionarEtapa("Segunda etapa");
        plano.adicionarEtapa("Terceira etapa");

        ReflectionTestUtils.setField(
            plano.getEtapas().get(0), "id", 10L
        );

        ReflectionTestUtils.setField(
            plano.getEtapas().get(1), "id", 20L
        );

        ReflectionTestUtils.setField(
            plano.getEtapas().get(2), "id", 30L
        );

        when(planoRepository.findById(1L))
            .thenReturn(Optional.of(plano));

        when(planoRepository.save(plano))
            .thenReturn(plano);

        service.reordenarEtapas(
            1L,
            List.of(30L, 10L, 20L)
        );

        assertEquals(
            2,
            plano.getEtapas().get(0).getOrdem()
        );

        assertEquals(
            3,
            plano.getEtapas().get(1).getOrdem()
        );

        assertEquals(
            1,
            plano.getEtapas().get(2).getOrdem()
        );

        verify(planoRepository).save(plano);
    }

    @Test
    void naoDeveReordenarEtapasDoPlanoAtivo() {

        PlanoTratamento plano =
            new PlanoTratamento("Tratamento", paciente);

        plano.adicionarEtapa("Primeira etapa");

        plano.alterarStatus(StatusPlanoTratamento.ATIVO);

        when(planoRepository.findById(1L))
            .thenReturn(Optional.of(plano));

        assertThrows(
            IllegalStateException.class,
            () -> service.reordenarEtapas(
                1L,
                List.of(10L)
            )
        );

        verify(planoRepository, never())
            .save(any(PlanoTratamento.class));
    }

    @Test
    void naoDeveReordenarComIdsRepetidos() {

        PlanoTratamento plano =
            new PlanoTratamento("Tratamento", paciente);

        plano.adicionarEtapa("Primeira etapa");
        plano.adicionarEtapa("Segunda etapa");

        when(planoRepository.findById(1L))
            .thenReturn(Optional.of(plano));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.reordenarEtapas(
                1L,
                List.of(10L, 10L)
            )
        );

        verify(planoRepository, never())
            .save(any(PlanoTratamento.class));
    }

    // TESTES DE STATUS

    @Test
    void deveAlterarStatusParaAtivo() {

        PlanoTratamento plano =
            new PlanoTratamento("Tratamento", paciente);

        when(planoRepository.findById(1L))
            .thenReturn(Optional.of(plano));

        when(planoRepository.save(plano))
            .thenReturn(plano);

        service.alterarStatus(
            1L,
            StatusPlanoTratamento.ATIVO
        );

        assertEquals(
            StatusPlanoTratamento.ATIVO,
            plano.getStatus()
        );

        verify(planoRepository).save(plano);
    }

    @Test
    void naoDeveAlterarStatusParaNull() {

        PlanoTratamento plano =
            new PlanoTratamento("Tratamento", paciente);

        when(planoRepository.findById(1L))
            .thenReturn(Optional.of(plano));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.alterarStatus(1L, null)
        );

        verify(planoRepository, never())
            .save(any(PlanoTratamento.class));
    }
}
