package com.godzilla.locadora.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corpo da requisicao de aluguel.
 *
 * <p>As anotacoes de Bean Validation sao verificadas pelo Spring antes de o
 * metodo do controller executar (por causa do {@code @Valid} na assinatura).
 * Um campo ausente vira 400 Bad Request automaticamente, sem nenhum {@code if}
 * escrito a mao.
 *
 * <p>Repare que NAO existe um campo {@code usuarioId} aqui. Quem esta alugando e
 * determinado pelo token JWT enviado no cabecalho {@code Authorization} — a
 * unica fonte confiavel. Aceitar o id do cliente no corpo permitiria que
 * qualquer um alugasse em nome de outro apenas trocando um numero.
 */
public record AlugarFilmeRequest(

        @NotNull(message = "filmeId é obrigatório")
        Long filmeId) {
}
