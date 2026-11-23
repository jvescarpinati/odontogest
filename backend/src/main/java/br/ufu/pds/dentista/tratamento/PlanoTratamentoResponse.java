package br.ufu.pds.dentista.tratamento;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record PlanoTratamentoResponse(
    Long id,
    String titulo,
    Long pacienteId,
    LocalDateTime criadoEm,
    StatusPlanoTratamento status,
    List<EtapaResponse> etapas
) {
    public record EtapaResponse(
        Long id,
        String procedimento,
        Integer ordem,
        StatusEtapa status
    ) {}

    public static PlanoTratamentoResponse from(PlanoTratamento plano) {
        List<EtapaResponse> etapas = plano.getEtapas().stream()
            .sorted(Comparator.comparing(EtapaTratamento::getOrdem))
            .map(e -> new EtapaResponse(
                e.getId(),
                e.getProcedimento(),
                e.getOrdem(),
                e.getStatus()
            ))
            .toList();

        return new PlanoTratamentoResponse(
            plano.getId(),
            plano.getTitulo(),
            plano.getPaciente().getId(),
            plano.getCriadoEm(),
            plano.getStatus(),
            etapas
        );
    }
}