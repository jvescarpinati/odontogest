package br.ufu.pds.dentista.common;

/** Vira HTTP 409 (ex.: CPF duplicado). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) { super(mensagem); }
}
