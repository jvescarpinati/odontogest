package br.ufu.pds.dentista.paciente;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Dados recebidos no POST e no PUT. */
public record PacienteRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "CPF é obrigatório")
        @Cpf
        String cpf,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve estar no passado")
        LocalDate dataNascimento,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "^[0-9()+\\-\\s]{8,20}$", message = "Telefone inválido")
        String telefone,

        @Email(message = "E-mail inválido")
        @Size(max = 150)
        String email,

        @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
        String endereco
) { }
