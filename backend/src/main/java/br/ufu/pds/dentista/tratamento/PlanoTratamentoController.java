package br.ufu.pds.dentista.tratamento;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planos-tratamento")
public class PlanoTratamentoController {

    private final PlanoTratamentoService service;

    public PlanoTratamentoController(PlanoTratamentoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanoTratamentoResponse criar(
            @RequestBody PlanoTratamentoRequest request) {

        return PlanoTratamentoResponse.from(
            service.criar(request.titulo(), request.pacienteId())
        );
    }

    @GetMapping("/{id}")
    public PlanoTratamentoResponse buscar(@PathVariable Long id) {
        return PlanoTratamentoResponse.from(service.buscar(id));
    }

    @GetMapping
    public List<PlanoTratamentoResponse> listar(
            @RequestParam Long pacienteId) {

        return service.listarPorPaciente(pacienteId).stream()
            .map(PlanoTratamentoResponse::from)
            .toList();
    }

    public record EtapaRequest(String procedimento) {}

    @PostMapping("/{id}/etapas")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanoTratamentoResponse adicionarEtapa(
            @PathVariable Long id,
            @RequestBody EtapaRequest request) {

        return PlanoTratamentoResponse.from(
            service.adicionarEtapa(id, request.procedimento())
        );
    }

    public record StatusRequest(StatusPlanoTratamento status) {}

    @PatchMapping("/{id}/status")
    public PlanoTratamentoResponse alterarStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request) {

        return PlanoTratamentoResponse.from(
            service.alterarStatus(id, request.status())
        );
    }

    public record ReordenarRequest(List<Long> idsOrdenados) {}

    @PutMapping("/{id}/etapas/ordem")
    public PlanoTratamentoResponse reordenar(
            @PathVariable Long id,
            @RequestBody ReordenarRequest request) {

        return PlanoTratamentoResponse.from(
            service.reordenarEtapas(id, request.idsOrdenados())
        );
    }

    @DeleteMapping("/{id}/etapas/{etapaId}")
    public PlanoTratamentoResponse removerEtapa(
            @PathVariable Long id,
            @PathVariable Long etapaId) {

        return PlanoTratamentoResponse.from(
            service.removerEtapa(id, etapaId)
        );
    }
}