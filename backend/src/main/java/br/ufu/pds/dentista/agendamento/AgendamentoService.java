
package br.ufu.pds.dentista.agendamento;

import br.ufu.pds.dentista.common.ConflitoException;
import br.ufu.pds.dentista.common.NaoEncontradoException;
import br.ufu.pds.dentista.paciente.Paciente;
import br.ufu.pds.dentista.paciente.PacienteRepository;
import br.ufu.pds.dentista.tratamento.EtapaTratamento;
import br.ufu.pds.dentista.tratamento.EtapaTratamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PacienteRepository pacienteRepository;
    private final EtapaTratamentoRepository etapaRepository;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            PacienteRepository pacienteRepository,
            EtapaTratamentoRepository etapaRepository) {

        this.agendamentoRepository = agendamentoRepository;
        this.pacienteRepository = pacienteRepository;
        this.etapaRepository = etapaRepository;
    }

    // Criação

    @Transactional
    public AgendamentoResponse criar(AgendamentoRequest req) {

        validarDatas(req.dataHoraInicio(), req.dataHoraFim());

        Paciente paciente = buscarPacienteAtivo(req.pacienteId());

        validarEtapa(req.etapaId(), paciente.getId());

        verificarConflito(
                req.dataHoraInicio(),
                req.dataHoraFim(),
                0L
        );

        Agendamento a = new Agendamento(
                paciente,
                req.dataHoraInicio(),
                req.dataHoraFim(),
                req.observacoes()
        );

        a.setEtapaId(req.etapaId());

        return AgendamentoResponse.de(
                agendamentoRepository.save(a)
        );
    }

    // Leitura

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listarPorIntervalo(
            LocalDateTime inicio,
            LocalDateTime fim) {

        if (inicio == null || fim == null || !fim.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "Intervalo de datas inválido"
            );
        }

        return agendamentoRepository.findByIntervalo(inicio, fim)
                .stream()
                .map(AgendamentoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listarPorPaciente(Long pacienteId) {

        buscarPacienteAtivo(pacienteId);

        return agendamentoRepository
                .findByPacienteIdOrderByDataHoraInicioAsc(pacienteId)
                .stream()
                .map(AgendamentoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse buscarPorId(Long id) {

        return AgendamentoResponse.de(
                buscarAgendamento(id)
        );
    }

    // Edição

    @Transactional
    public AgendamentoResponse atualizar(Long id, AgendamentoRequest req) {

        Agendamento a = buscarAgendamento(id);

        if (a.isCancelado()) {
            throw new ConflitoException(
                    "Não é possível alterar um agendamento cancelado"
            );
        }

        validarDatas(req.dataHoraInicio(), req.dataHoraFim());

        // Mantém o paciente original do agendamento.
        Long pacienteId = a.getPaciente().getId();

        // Confere se o paciente enviado é o mesmo.
        if (!pacienteId.equals(req.pacienteId())) {
            throw new IllegalArgumentException(
                    "Não é permitido alterar o paciente do agendamento."
            );
        }

        validarEtapa(req.etapaId(), pacienteId);

        verificarConflito(
                req.dataHoraInicio(),
                req.dataHoraFim(),
                id
        );

        a.atualizar(
                req.dataHoraInicio(),
                req.dataHoraFim(),
                req.observacoes()
        );

        a.setEtapaId(req.etapaId());

        return AgendamentoResponse.de(a);
    }

    // Mudança de status

    @Transactional
    public AgendamentoResponse cancelar(Long id) {

        Agendamento a = buscarAgendamento(id);

        if (a.isCancelado()) {
            throw new ConflitoException(
                    "Agendamento já está cancelado"
            );
        }

        a.cancelar();

        return AgendamentoResponse.de(a);
    }

    @Transactional
    public AgendamentoResponse confirmar(Long id) {

        Agendamento a = buscarAgendamento(id);

        a.confirmar();

        return AgendamentoResponse.de(a);
    }

    @Transactional
    public AgendamentoResponse realizar(Long id) {

        Agendamento a = buscarAgendamento(id);

        a.realizar();

        return AgendamentoResponse.de(a);
    }

    // Helpers privados

    private Agendamento buscarAgendamento(Long id) {

        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException(
                        "Agendamento não encontrado"
                ));
    }

    private Paciente buscarPacienteAtivo(Long pacienteId) {

        return pacienteRepository.findByIdAndAtivoTrue(pacienteId)
                .orElseThrow(() -> new NaoEncontradoException(
                        "Paciente não encontrado"
                ));
    }

    /**
     * Valida a etapa de tratamento vinculada ao agendamento.
     *
     * A etapa é opcional.
     * Quando informada, deve existir e pertencer ao mesmo paciente.
     */
    private void validarEtapa(Long etapaId, Long pacienteId) {

        if (etapaId == null) {
            return;
        }

        EtapaTratamento etapa = etapaRepository.findById(etapaId)
                .orElseThrow(() -> new NaoEncontradoException(
                        "Etapa de tratamento não encontrada."
                ));

        Long pacienteDoPlanoId = etapa.getPlano()
                .getPaciente()
                .getId();

        if (!pacienteId.equals(pacienteDoPlanoId)) {
            throw new IllegalArgumentException(
                    "A etapa de tratamento não pertence ao paciente do agendamento."
            );
        }
    }

    /**
     * Valida que o fim é posterior ao início.
     */
    private void validarDatas(
            LocalDateTime inicio,
            LocalDateTime fim) {

        if (inicio == null || fim == null || !fim.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "A data/hora de fim deve ser posterior à data/hora de início"
            );
        }
    }

    /**
     * Verifica conflitos de horários.
     */
    private void verificarConflito(
            LocalDateTime inicio,
            LocalDateTime fim,
            Long excludeId) {

        long conflitos = agendamentoRepository.contarConflitos(
                inicio,
                fim,
                excludeId,
                StatusAgendamento.CANCELADA
        );

        if (conflitos > 0) {
            throw new ConflitoException(
                    "Já existe um agendamento ativo neste horário. "
                    + "Verifique a agenda e escolha outro horário."
            );
        }
    }
}
