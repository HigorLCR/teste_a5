package com.godzilla.locadora.exception;

/**
 * Lancada quando um identificador informado nao corresponde a nenhum registro.
 *
 * <p>Mapeada para 404 em {@link TratadorGlobalDeErros}.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
