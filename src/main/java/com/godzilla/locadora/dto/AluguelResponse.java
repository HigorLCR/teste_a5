package com.godzilla.locadora.dto;

import com.godzilla.locadora.domain.Aluguel;
import java.time.Instant;

/** Comprovante do aluguel realizado. */
public record AluguelResponse(
        Long aluguelId,
        Long filmeId,
        String titulo,
        Long usuarioId,
        Instant alugadoEm) {

    public static AluguelResponse de(Aluguel aluguel) {
        return new AluguelResponse(
                aluguel.getId(),
                aluguel.getFilme().getId(),
                aluguel.getFilme().getTitulo(),
                aluguel.getUsuario().getId(),
                aluguel.getAlugadoEm());
    }
}
