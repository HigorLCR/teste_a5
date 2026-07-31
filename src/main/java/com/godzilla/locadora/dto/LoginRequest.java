package com.godzilla.locadora.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corpo da requisicao de login, no formato do exemplo do enunciado:
 * {@code {"email": "cliente@cliente.com", "senha": "123@"}}.
 */
public record LoginRequest(

        @NotBlank(message = "email é obrigatório")
        String email,

        @NotBlank(message = "senha é obrigatória")
        String senha) {
}
