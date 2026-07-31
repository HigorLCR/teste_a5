package com.godzilla.locadora.exception;

/** Identificador informado nao corresponde a nenhum registro. Mapeada para 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
