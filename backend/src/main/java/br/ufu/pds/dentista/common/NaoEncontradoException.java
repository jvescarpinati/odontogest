package br.ufu.pds.dentista.common;

/** Vira HTTP 404. */
public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String mensagem) { super(mensagem); }
}
