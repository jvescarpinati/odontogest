package br.ufu.pds.dentista.agendamento;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO de entrada para criação e edição de agendamentos (POST e PUT).
 *
 * {@code etapaId} é omitido intencionalmente até que a US03 seja implementada.
 */
public record AgendamentoRequest(

        @NotNull(message = "Paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "Data/hora de início é obrigatória")
        @Future(message = "O agendamento deve ser para uma data futura")
        LocalDateTime dataHoraInicio,

        @NotNull(message = "Data/hora de fim é obrigatória")
        LocalDateTime dataHoraFim,

        String observacoes
) { }
