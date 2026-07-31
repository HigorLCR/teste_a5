package com.godzilla.locadora.controller;

import com.godzilla.locadora.dto.ListaFilmesResponse;
import com.godzilla.locadora.service.FilmeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Teste 2 do desafio: consulta de filmes do catalogo. */
@RestController
public class FilmeController {

    private final FilmeService filmeService;

    public FilmeController(FilmeService filmeService) {
        this.filmeService = filmeService;
    }

    /**
     * Retorna os filmes cujo titulo contenha o termo informado.
     *
     * <p>O enunciado grafa o caminho como {@code localdora/godzilla}, provavel
     * erro de digitacao; o endpoint responde nas duas grafias.
     */
    @Operation(summary = "Busca filmes no catalogo",
            description = "Filtra por parte do titulo, ignorando maiusculas, e/ou por ano — nao e "
                    + "preciso informar o titulo inteiro. Sem parametros, devolve o catalogo completo.")
    @GetMapping({"/locadora/godzilla", "/localdora/godzilla"})
    public ListaFilmesResponse buscar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Short ano) {

        return new ListaFilmesResponse(filmeService.buscar(titulo, ano));
    }
}
