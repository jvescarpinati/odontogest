package br.ufu.pds.dentista.paciente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Regra: o CPF deve ter formato válido (11 dígitos e dígitos verificadores corretos). */
@DisplayName("CpfUtils - validação de CPF")
class CpfUtilsTest {

    @ParameterizedTest(name = "\"{0}\" é válido")
    @ValueSource(strings = {"529.982.247-25", "52998224725", "111.444.777-35", "123.456.789-09"})
    void aceitaCpfsValidosComOuSemMascara(String cpf) {
        assertThat(CpfUtils.isValido(cpf)).isTrue();
    }

    @ParameterizedTest(name = "\"{0}\" é inválido")
    @ValueSource(strings = {
            "529.982.247-24",  // 2º dígito verificador errado
            "529.982.247-15",  // 1º dígito verificador errado
            "123.456.789-00"   // ambos errados
    })
    void rejeitaDigitosVerificadoresIncorretos(String cpf) {
        assertThat(CpfUtils.isValido(cpf)).isFalse();
    }

    @ParameterizedTest(name = "\"{0}\" é inválido")
    @ValueSource(strings = {"000.000.000-00", "111.111.111-11", "99999999999"})
    void rejeitaSequenciasDeDigitosRepetidos(String cpf) {
        // essas sequências passam no cálculo do módulo 11, mas não são CPFs reais
        assertThat(CpfUtils.isValido(cpf)).isFalse();
    }

    @ParameterizedTest(name = "\"{0}\" é inválido")
    @ValueSource(strings = {"123", "5299822472", "529982247250", "abc.def.ghi-jk"})
    void rejeitaTamanhoDiferenteDeOnzeDigitos(String cpf) {
        assertThat(CpfUtils.isValido(cpf)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejeitaNuloEVazio(String cpf) {
        assertThat(CpfUtils.isValido(cpf)).isFalse();
    }

    @Test
    void normalizarRemoveMascaraEMantemSoDigitos() {
        assertThat(CpfUtils.normalizar("529.982.247-25")).isEqualTo("52998224725");
        assertThat(CpfUtils.normalizar(" 529 982 247 25 ")).isEqualTo("52998224725");
        assertThat(CpfUtils.normalizar(null)).isNull();
    }
}
