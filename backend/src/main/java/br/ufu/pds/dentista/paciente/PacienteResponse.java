package br.ufu.pds.dentista.paciente;

import java.time.LocalDate;

/** O que a API devolve. Não expõe a entidade diretamente. */
public record PacienteResponse(
        Long id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String telefone,
        String email,
        String endereco
) {
    public static PacienteResponse de(Paciente p) {
        return new PacienteResponse(p.getId(), p.getNome(), p.getCpf(), p.getDataNascimento(),
                p.getTelefone(), p.getEmail(), p.getEndereco());
    }
}
