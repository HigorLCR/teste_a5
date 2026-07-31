package com.godzilla.locadora.exception;

/**
 * Lancada quando e-mail ou senha nao conferem no login. Mapeada para 401 em
 * {@link TratadorGlobalDeErros}.
 *
 * <p>A mensagem e deliberadamente generica e identica nos dois casos — e-mail
 * inexistente e senha errada. Responder "e-mail nao cadastrado" entregaria a um
 * atacante a confirmacao de quais e-mails existem na base, transformando o login
 * em um verificador de cadastros.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }
}
