package br.ufu.pds.dentista.anamnese;

import java.time.LocalDateTime;

public record AnamneseDTO(
    Long id,
    Long pacienteId,
    String pacienteNome,
    String alergias,
    String condicoesPreexistentes,
    String medicamentosEmUso,
    String alertasGraves,
    LocalDateTime dataModificacao
) {}
