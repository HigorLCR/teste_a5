package com.godzilla.locadora.exception;

/**
 * E-mail ou senha nao conferem. Mapeada para 401, com mensagem generica e
 * identica nos dois casos, para nao permitir descobrir quais e-mails existem.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }
}
