package com.godzilla.locadora.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo da requisicao de cadastro de um novo cliente. */
public record CadastroUsuarioRequest(

        @NotBlank(message = "nome é obrigatório")
        @Size(max = 150, message = "nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        @Size(max = 180, message = "email deve ter no máximo 180 caracteres")
        String email,

        @NotBlank(message = "senha é obrigatória")
        @Size(min = 4, max = 72, message = "senha deve ter entre 4 e 72 caracteres")
        String senha) {

    // O limite de 72 nao e arbitrario: o algoritmo BCrypt ignora silenciosamente
    // tudo o que passa de 72 bytes. Rejeitar explicitamente e melhor do que
    // aceitar uma senha longa e truncar parte dela sem o usuario saber.
}
