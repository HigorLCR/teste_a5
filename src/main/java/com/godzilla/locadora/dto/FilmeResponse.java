package com.godzilla.locadora.dto;

import com.godzilla.locadora.domain.Filme;

/**
 * Representacao de um filme na resposta da API.
 *
 * <p>E um {@code record}: imutavel, com equals/hashCode/toString gerados pelo
 * compilador e sem uma linha de boilerplate.
 *
 * <p>Existe um DTO separado da entidade de proposito. Serializar a entidade
 * {@link Filme} diretamente acoplaria o contrato publico da API ao schema do
 * banco — qualquer coluna nova vazaria para os clientes, e relacoes lazy
 * quebrariam a serializacao.
 *
 * <p>O nome {@code filmeId} segue o exemplo do enunciado, que traz
 * {@code "filmesId"} em um item e {@code "filmeId"} em outro; adotamos a segunda
 * grafia, no singular, e a mantemos consistente.
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
