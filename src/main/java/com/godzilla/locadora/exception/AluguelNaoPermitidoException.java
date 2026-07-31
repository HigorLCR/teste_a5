package com.godzilla.locadora.exception;

/**
 * Lancada quando o aluguel solicitado nao pode ser realizado — por falta de
 * estoque ou porque o cliente ja possui um filme em maos.
 *
 * <p>O status HTTP correspondente e definido em {@link TratadorGlobalDeErros},
 * nao aqui: esta classe pertence ao dominio e nao deveria conhecer HTTP.
 *
 * <p>O enunciado determina: "Caso o aluguel do filme escolhido seja permitido,
 * voce deve retornar HTTP 200-OK, caso contrario um HTTP 403-FORBIDDEN". Por
 * isso o mapeamento para 403, ainda que 409 Conflict fosse semanticamente mais
 * preciso — 403 trata de autorizacao, nao de estado do recurso. O requisito
 * explicito prevalece sobre a preferencia semantica, e a observacao fica
 * registrada.
 */
public class AluguelNaoPermitidoException extends RuntimeException {

    public AluguelNaoPermitidoException(String mensagem) {
        super(mensagem);
    }
}
