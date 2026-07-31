package com.godzilla.locadora.dto;

import jakarta.validation.constraints.NotBlank;

/** Corpo do login, no formato do exemplo do enunciado. */
public record LoginRequest(

        @NotBlank(message = "email é obrigatório")
        String email,

        @NotBlank(message = "senha é obrigatória")
        String senha) {
}
