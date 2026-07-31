package com.godzilla.locadora.dto;

import com.godzilla.locadora.domain.Aluguel;
import java.time.Instant;

/**
 * Comprovante da devolucao realizada, com o periodo completo da locacao.
 */
public record DevolucaoResponse(
        Long aluguelId,
        Long filmeId,
        String titulo,
        Long usuarioId,
        Instant alugadoEm,
        Instant devolvidoEm) {

    /**
     * {@code devolvidoEm} vem por parametro: o fechamento foi um UPDATE direto,
     * entao a entidade em memoria ainda tem o campo nulo.
     */
    public static DevolucaoResponse de(Aluguel aluguel, Instant devolvidoEm) {
        return new DevolucaoResponse(
                aluguel.getId(),
                aluguel.getFilme().getId(),
                aluguel.getFilme().getTitulo(),
                aluguel.getUsuario().getId(),
                aluguel.getAlugadoEm(),
                devolvidoEm);
    }
}
