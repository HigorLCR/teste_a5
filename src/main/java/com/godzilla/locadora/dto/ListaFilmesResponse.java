package com.godzilla.locadora.dto;

import java.util.List;

/**
 * Envelope da resposta de busca.
 *
 * <p>Produz {@code {"filmes": [ ... ]}}, o formato mostrado no enunciado.
 *
 * <p>Envelopar em um objeto, em vez de devolver um array cru no topo do JSON,
 * tambem permite acrescentar metadados (total, pagina) no futuro sem quebrar os
 * clientes que ja consomem a API.
 */
public record ListaFilmesResponse(List<FilmeResponse> filmes) {
}
