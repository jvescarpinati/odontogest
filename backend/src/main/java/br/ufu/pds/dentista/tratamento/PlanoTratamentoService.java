
package br.ufu.pds.dentista.tratamento;

import br.ufu.pds.dentista.agendamento.AgendamentoRepository;
import br.ufu.pds.dentista.paciente.Paciente;
import br.ufu.pds.dentista.paciente.PacienteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PlanoTratamentoService {

    private final PlanoTratamentoRepository planoRepository;
    private final PacienteRepository pacienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    public PlanoTratamentoService(
            PlanoTratamentoRepository planoRepository,
            PacienteRepository pacienteRepository,
            AgendamentoRepository agendamentoRepository) {

        this.planoRepository = planoRepository;
        this.pacienteRepository = pacienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional
    public PlanoTratamento criar(String titulo, Long pacienteId) {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                "O título do plano é obrigatório."
            );
        }

        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new NoSuchElementException(
                "Paciente não encontrado."
            ));

        PlanoTratamento plano =
            new PlanoTratamento(titulo.trim(), paciente);

        return planoRepository.save(plano);
    }

    @Transactional(readOnly = true)
    public PlanoTratamento buscar(Long id) {

        return planoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(
                "Plano de tratamento não encontrado."
            ));
    }

    @Transactional(readOnly = true)
    public List<PlanoTratamento> listarPorPaciente(Long pacienteId) {

        return planoRepository.findByPacienteId(pacienteId);
    }

    @Transactional
    public PlanoTratamento adicionarEtapa(
            Long planoId, String procedimento) {

        PlanoTratamento plano = buscar(planoId);

        if (procedimento == null || procedimento.isBlank()) {
            throw new IllegalArgumentException(
                "O procedimento é obrigatório."
            );
        }

        plano.adicionarEtapa(procedimento.trim());

        return planoRepository.save(plano);
    }

    @Transactional
    public PlanoTratamento alterarStatus(
            Long planoId, StatusPlanoTratamento status) {

        PlanoTratamento plano = buscar(planoId);

        if (status == null) {
            throw new IllegalArgumentException(
                "O status é obrigatório."
            );
        }

        plano.alterarStatus(status);

        return planoRepository.save(plano);
    }

    @Transactional
    public PlanoTratamento reordenarEtapas(
            Long planoId, List<Long> idsOrdenados) {

        PlanoTratamento plano = buscar(planoId);
        plano.verificarEmElaboracao();

        List<EtapaTratamento> etapas = plano.getEtapas();

        if (idsOrdenados == null ||
            idsOrdenados.size() != etapas.size() ||
            idsOrdenados.stream().distinct().count() != etapas.size()) {

            throw new IllegalArgumentException(
                "A lista deve conter todas as etapas, sem repetição."
            );
        }

        for (Long id : idsOrdenados) {

            boolean existe = etapas.stream()
                .anyMatch(etapa -> etapa.getId().equals(id));

            if (!existe) {
                throw new IllegalArgumentException(
                    "Etapa não pertence ao plano."
                );
            }
        }

        for (int i = 0; i < idsOrdenados.size(); i++) {

            Long id = idsOrdenados.get(i);

            for (EtapaTratamento etapa : etapas) {

                if (etapa.getId().equals(id)) {
                    etapa.alterarOrdem(i + 1);
                    break;
                }
            }
        }

        return planoRepository.save(plano);
    }

    @Transactional
    public PlanoTratamento removerEtapa(
            Long planoId, Long etapaId) {

        PlanoTratamento plano = buscar(planoId);

        // Só permite remover etapas de planos em elaboração.
        plano.verificarEmElaboracao();

        // Confere se a etapa realmente pertence ao plano.
        boolean existe = plano.getEtapas().stream()
            .anyMatch(etapa -> etapa.getId().equals(etapaId));

        if (!existe) {
            throw new NoSuchElementException(
                "Etapa não encontrada neste plano."
            );
        }

        // Integração US03 + US04:
        // impede excluir uma etapa vinculada a um agendamento.
        if (agendamentoRepository.existsByEtapaId(etapaId)) {
            throw new IllegalStateException(
                "Não é possível remover uma etapa vinculada a um agendamento."
            );
        }

        // Remove a etapa do plano.
        plano.getEtapas()
            .removeIf(etapa -> etapa.getId().equals(etapaId));

        // Reorganiza a ordem das etapas restantes.
        int ordem = 1;

        for (EtapaTratamento etapa : plano.getEtapas()) {
            etapa.alterarOrdem(ordem++);
        }

        return planoRepository.save(plano);
    }
}
