package com.godzilla.locadora.dto;

import com.godzilla.locadora.domain.Filme;

/**
 * Um filme na resposta da API. O enunciado alterna {@code filmesId} e
 * {@code filmeId}; adotamos a segunda grafia.
 */
public record FilmeResponse(
        Long filmeId,
        String titulo,
        String diretor,
        Short ano,
        int estoque) {

    public static FilmeResponse de(Filme filme) {
        return new FilmeResponse(
                filme.getId(),
                filme.getTitulo(),
                filme.getDiretor(),
                filme.getAno(),
                filme.getEstoque());
    }
}
