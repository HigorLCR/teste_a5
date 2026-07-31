package com.godzilla.locadora.exception;

/** E-mail do cadastro ja pertence a outro cliente. Mapeada para 409. */
public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}
