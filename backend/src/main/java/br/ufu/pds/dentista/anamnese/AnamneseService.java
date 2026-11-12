package br.ufu.pds.dentista.anamnese;

import br.ufu.pds.dentista.common.NaoEncontradoException;
import br.ufu.pds.dentista.paciente.Paciente;
import br.ufu.pds.dentista.paciente.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AnamneseService {

    private final AnamneseRepository anamneseRepository;
    private final PacienteRepository pacienteRepository;

    public AnamneseService(AnamneseRepository anamneseRepository, PacienteRepository pacienteRepository) {
        this.anamneseRepository = anamneseRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional(readOnly = true)
    public AnamneseDTO buscarPorPaciente(Long pacienteId) {
        Anamnese a = anamneseRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new NaoEncontradoException("Anamnese não encontrada para o paciente"));
        return toDTO(a);
    }

    @Transactional
    public AnamneseDTO salvar(AnamneseDTO dto) {
        Paciente paciente = pacienteRepository.findByIdAndAtivoTrue(dto.pacienteId())
                .orElseThrow(() -> new NaoEncontradoException("Paciente não encontrado"));

        Anamnese a = anamneseRepository.findByPacienteId(dto.pacienteId())
                .orElseGet(() -> new Anamnese(paciente, null, null, null, null));

        a.setAlergias(dto.alergias());
        a.setCondicoesPreexistentes(dto.condicoesPreexistentes());
        a.setMedicamentosEmUso(dto.medicamentosEmUso());
        a.setAlertasGraves(dto.alertasGraves());
        a.setDataModificacao(LocalDateTime.now());

        return toDTO(anamneseRepository.save(a));
    }

    private AnamneseDTO toDTO(Anamnese a) {
        return new AnamneseDTO(
            a.getId(),
            a.getPaciente().getId(),
            a.getPaciente().getNome(),
            a.getAlergias(),
            a.getCondicoesPreexistentes(),
            a.getMedicamentosEmUso(),
            a.getAlertasGraves(),
            a.getDataModificacao()
        );
    }
}
