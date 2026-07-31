package com.godzilla.locadora.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corpo da requisicao de aluguel. Nao ha {@code usuarioId}: quem esta alugando
 * vem do token, unica fonte confiavel.
 */
public record AlugarFilmeRequest(

        @NotNull(message = "filmeId é obrigatório")
        Long filmeId) {
}
