package com.godzilla.locadora.dto;

import java.util.List;

/** Produz {@code {"filmes": [ ... ]}}, o formato mostrado no enunciado. */
public record ListaFilmesResponse(List<FilmeResponse> filmes) {
}
