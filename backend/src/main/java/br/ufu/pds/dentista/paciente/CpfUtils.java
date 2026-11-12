package br.ufu.pds.dentista.paciente;

public final class CpfUtils {

    private CpfUtils() { }

    /** Remove máscara: "123.456.789-09" -> "12345678909". */
    public static String normalizar(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    /**
     * Valida o CPF pelos dígitos verificadores (módulo 11).
     * 1º DV: soma dos 9 primeiros dígitos com pesos 10..2.
     * 2º DV: soma dos 10 primeiros dígitos com pesos 11..2.
     * Em cada caso: resto = (soma * 10) % 11; se resto == 10, DV = 0.
     */
    public static boolean isValido(String cpf) {
        String d = normalizar(cpf);
        if (d == null || d.length() != 11) return false;
        if (d.chars().distinct().count() == 1) return false; // 000..., 111..., etc. passam no cálculo mas são inválidos

        return d.charAt(9) - '0' == calcularDigito(d, 9)
            && d.charAt(10) - '0' == calcularDigito(d, 10);
    }

    private static int calcularDigito(String d, int quantidade) {
        int soma = 0;
        int peso = quantidade + 1;
        for (int i = 0; i < quantidade; i++) {
            soma += (d.charAt(i) - '0') * peso--;
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
