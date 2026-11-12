package br.ufu.pds.dentista.anamnese;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anamneses")
public class AnamneseController {

    private final AnamneseService service;

    public AnamneseController(AnamneseService service) {
        this.service = service;
    }

    @GetMapping("/paciente/{pacienteId}")
    public AnamneseDTO buscar(@PathVariable Long pacienteId) {
        return service.buscarPorPaciente(pacienteId);
    }

    @PostMapping
    public AnamneseDTO salvar(@RequestBody AnamneseDTO dto) {
        return service.salvar(dto);
    }
}
