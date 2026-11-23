package br.ufu.pds.dentista.agendamento;

import java.time.LocalDateTime;

/**
 * DTO de saída — nunca expõe a entidade JPA diretamente.
 * Inclui nome do paciente para facilitar a renderização da agenda no frontend.
 */
public record AgendamentoResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        Long etapaId,                   // sempre null até que a US03 seja implementada
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        String status,
        String observacoes,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static AgendamentoResponse de(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(),
                a.getPaciente().getId(),
                a.getPaciente().getNome(),
                a.getEtapaId(),
                a.getDataHoraInicio(),
                a.getDataHoraFim(),
                a.getStatus().name(),
                a.getObservacoes(),
                a.getCriadoEm(),
                a.getAtualizadoEm()
        );
    }
}
