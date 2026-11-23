package br.ufu.pds.dentista.tratamento;

public record PlanoTratamentoRequest(
    String titulo,
    Long pacienteId
) {}