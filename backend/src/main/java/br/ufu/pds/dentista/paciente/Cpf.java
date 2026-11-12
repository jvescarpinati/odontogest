package br.ufu.pds.dentista.paciente;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/** Bean Validation: @Cpf em um campo String valida formato e dígitos verificadores. */
@Documented
@Constraint(validatedBy = Cpf.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cpf {

    String message() default "CPF inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<Cpf, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext ctx) {
            // null/vazio é responsabilidade do @NotBlank; aqui só validamos o formato
            return value == null || value.isBlank() || CpfUtils.isValido(value);
        }
    }
}
