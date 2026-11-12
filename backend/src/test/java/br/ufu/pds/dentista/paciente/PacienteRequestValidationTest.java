package br.ufu.pds.dentista.paciente;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regras de validação dos dados cadastrais (critérios de aceitação da US01).
 * Teste unitário puro: usa o Bean Validation diretamente, sem subir o Spring.
 */
@DisplayName("PacienteRequest - regras de validação cadastral")
class PacienteRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void criarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidador() {
        factory.close();
    }

    // ---------- helpers ----------

    private static PacienteRequest req(String nome, String cpf, LocalDate nascimento,
                                       String telefone, String email, String endereco) {
        return new PacienteRequest(nome, cpf, nascimento, telefone, email, endereco);
    }

    private static PacienteRequest valido() {
        return req("Maria Silva", "529.982.247-25", LocalDate.of(1990, 5, 20),
                "(34) 99999-0000", "maria@email.com", "Rua X, 10 - Centro");
    }

    /** campo -> mensagem de erro */
    private static Map<String, String> erros(PacienteRequest r) {
        Set<ConstraintViolation<PacienteRequest>> v = validator.validate(r);
        return v.stream().collect(Collectors.toMap(
                c -> c.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (a, b) -> a));
    }

    // ---------- cenário feliz ----------

    @Test
    void pacienteComTodosOsCamposCorretosNaoTemErros() {
        assertThat(erros(valido())).isEmpty();
    }

    @Test
    void emailEEnderecoSaoOpcionais() {
        var r = req("Maria Silva", "52998224725", LocalDate.of(1990, 5, 20), "34999990000", null, null);
        assertThat(erros(r)).isEmpty();
    }

    // ---------- campos obrigatórios ----------

    @Test
    void nomeEObrigatorio() {
        var r = req("   ", "52998224725", LocalDate.of(1990, 5, 20), "34999990000", null, null);
        assertThat(erros(r)).containsEntry("nome", "Nome é obrigatório");
    }

    @Test
    void cpfEObrigatorio() {
        var r = req("Maria", "", LocalDate.of(1990, 5, 20), "34999990000", null, null);
        // só a mensagem de obrigatório: o @Cpf não acusa erro em campo vazio
        assertThat(erros(r)).containsExactly(Map.entry("cpf", "CPF é obrigatório"));
    }

    @Test
    void dataDeNascimentoEObrigatoria() {
        var r = req("Maria", "52998224725", null, "34999990000", null, null);
        assertThat(erros(r)).containsEntry("dataNascimento", "Data de nascimento é obrigatória");
    }

    @Test
    void telefoneEObrigatorio() {
        var r = req("Maria", "52998224725", LocalDate.of(1990, 5, 20), null, null, null);
        assertThat(erros(r)).containsEntry("telefone", "Telefone é obrigatório");
    }

    @Test
    void todosOsObrigatoriosAusentesGeramQuatroErros() {
        var r = req(null, null, null, null, null, null);
        assertThat(erros(r)).containsOnlyKeys("nome", "cpf", "dataNascimento", "telefone");
    }

    // ---------- formato ----------

    @Test
    void cpfComDigitoVerificadorErradoEInvalido() {
        var r = req("Maria", "529.982.247-24", LocalDate.of(1990, 5, 20), "34999990000", null, null);
        assertThat(erros(r)).containsEntry("cpf", "CPF inválido");
    }

    @Test
    void dataDeNascimentoNoFuturoEInvalida() {
        var r = req("Maria", "52998224725", LocalDate.now().plusDays(1), "34999990000", null, null);
        assertThat(erros(r)).containsKey("dataNascimento");
    }

    @Test
    void dataDeNascimentoHojeEInvalida() {
        // @Past exige data estritamente anterior a hoje
        var r = req("Maria", "52998224725", LocalDate.now(), "34999990000", null, null);
        assertThat(erros(r)).containsKey("dataNascimento");
    }

    @Test
    void telefoneComLetrasEInvalido() {
        var r = req("Maria", "52998224725", LocalDate.of(1990, 5, 20), "telefone-abc", null, null);
        assertThat(erros(r)).containsEntry("telefone", "Telefone inválido");
    }

    @Test
    void telefoneMuitoCurtoEInvalido() {
        var r = req("Maria", "52998224725", LocalDate.of(1990, 5, 20), "1234", null, null);
        assertThat(erros(r)).containsEntry("telefone", "Telefone inválido");
    }

    @Test
    void emailMalFormadoEInvalido() {
        var r = req("Maria", "52998224725", LocalDate.of(1990, 5, 20), "34999990000", "maria.email.com", null);
        assertThat(erros(r)).containsEntry("email", "E-mail inválido");
    }

    // ---------- tamanho máximo ----------

    @Test
    void nomeComMaisDe150CaracteresEInvalido() {
        var r = req("a".repeat(151), "52998224725", LocalDate.of(1990, 5, 20), "34999990000", null, null);
        assertThat(erros(r)).containsKey("nome");
    }

    @Test
    void enderecoComMaisDe255CaracteresEInvalido() {
        var r = req("Maria", "52998224725", LocalDate.of(1990, 5, 20), "34999990000", null, "x".repeat(256));
        assertThat(erros(r)).containsKey("endereco");
    }
}
