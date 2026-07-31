package com.godzilla.locadora.controller;

import com.godzilla.locadora.dto.ListaFilmesResponse;
import com.godzilla.locadora.service.FilmeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Teste 2 do desafio: consulta de filmes do catalogo.
 *
 * <p>O controller e uma casca fina. Ele traduz HTTP para chamadas de servico e
 * de volta — nao contem regra de negocio nenhuma.
 */
@RestController
public class FilmeController {

    private final FilmeService filmeService;

    public FilmeController(FilmeService filmeService) {
        this.filmeService = filmeService;
    }

    /**
     * Retorna os filmes cujo titulo contenha o termo informado.
     *
     * <p>O enunciado grafa o caminho como {@code "localdora/godzilla"}, que e
     * quase certamente um erro de digitacao de {@code "locadora"}. Em vez de
     * escolher entre obedecer o texto e escrever o correto, o endpoint responde
     * nos dois caminhos: o correto e o literal do enunciado.
     *
     * <p>Exemplos:
     * <pre>
     *   GET /locadora/godzilla?titulo=mechagodzilla
     *   GET /locadora/godzilla?ano=1964
     *   GET /locadora/godzilla?titulo=godzilla&amp;ano=1992
     *   GET /locadora/godzilla
     * </pre>
     */
    @GetMapping({"/locadora/godzilla", "/localdora/godzilla"})
    public ListaFilmesResponse buscar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Short ano) {

        return new ListaFilmesResponse(filmeService.buscar(titulo, ano));
    }
}
