package br.ufu.pds.dentista.agendamento;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    // ── POST /api/agendamentos ────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<AgendamentoResponse> criar(@Valid @RequestBody AgendamentoRequest req) {
        AgendamentoResponse criado = service.criar(req);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    // ── GET /api/agendamentos?inicio=...&fim=... ──────────────────────────────
    // Visão diária:  ?inicio=2025-06-01T00:00&fim=2025-06-02T00:00
    // Visão semanal: ?inicio=2025-06-01T00:00&fim=2025-06-08T00:00

    @GetMapping
    public List<AgendamentoResponse> listarPorIntervalo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return service.listarPorIntervalo(inicio, fim);
    }

    // ── GET /api/agendamentos/paciente/{pacienteId} ───────────────────────────

    @GetMapping("/paciente/{pacienteId}")
    public List<AgendamentoResponse> listarPorPaciente(@PathVariable Long pacienteId) {
        return service.listarPorPaciente(pacienteId);
    }

    // ── GET /api/agendamentos/{id} ────────────────────────────────────────────

    @GetMapping("/{id}")
    public AgendamentoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // ── PUT /api/agendamentos/{id} ────────────────────────────────────────────

    @PutMapping("/{id}")
    public AgendamentoResponse atualizar(@PathVariable Long id,
                                         @Valid @RequestBody AgendamentoRequest req) {
        return service.atualizar(id, req);
    }

    // ── PATCH /api/agendamentos/{id}/cancelar ─────────────────────────────────

    @PatchMapping("/{id}/cancelar")
    public AgendamentoResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    // ── PATCH /api/agendamentos/{id}/confirmar ────────────────────────────────

    @PatchMapping("/{id}/confirmar")
    public AgendamentoResponse confirmar(@PathVariable Long id) {
        return service.confirmar(id);
    }

    // ── PATCH /api/agendamentos/{id}/realizar ─────────────────────────────────

    @PatchMapping("/{id}/realizar")
    @ResponseStatus(HttpStatus.OK)
    public AgendamentoResponse realizar(@PathVariable Long id) {
        return service.realizar(id);
    }
}
