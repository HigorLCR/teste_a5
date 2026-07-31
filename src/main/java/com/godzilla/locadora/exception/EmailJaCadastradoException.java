package com.godzilla.locadora.exception;

/**
 * Lancada quando o e-mail informado no cadastro ja pertence a outro cliente.
 *
 * <p>Mapeada para 409 Conflict em {@link TratadorGlobalDeErros}: a requisicao
 * esta bem formada, mas colide com o estado atual do recurso.
 */
public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}
