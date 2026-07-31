package com.godzilla.locadora.exception;

/**
 * Aluguel recusado por falta de estoque ou porque o cliente ja possui um filme.
 * Mapeada para 403 por exigencia explicita do enunciado, ainda que 409 fosse
 * semanticamente mais preciso.
 */
public class AluguelNaoPermitidoException extends RuntimeException {

    public AluguelNaoPermitidoException(String mensagem) {
        super(mensagem);
    }
}
