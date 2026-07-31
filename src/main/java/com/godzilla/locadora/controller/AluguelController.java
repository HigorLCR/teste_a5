package com.godzilla.locadora.controller;

import com.godzilla.locadora.dto.AlugarFilmeRequest;
import com.godzilla.locadora.dto.AluguelResponse;
import com.godzilla.locadora.service.AluguelService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Teste 1 do desafio: aluguel de filmes.
 */
@RestController
public class AluguelController {

    private final AluguelService aluguelService;

    public AluguelController(AluguelService aluguelService) {
        this.aluguelService = aluguelService;
    }

    /**
     * Aluga um filme para o cliente autenticado.
     *
     * <p>O enunciado especifica o caminho {@code "/godzilla"} e os codigos de
     * resposta: 200 OK quando o aluguel e permitido, 403 FORBIDDEN caso
     * contrario. O verbo nao e especificado; POST e o correto, porque a
     * requisicao CRIA um recurso (o aluguel) e nao e idempotente.
     *
     * <p>A identidade do cliente vem do {@code subject} do JWT, preenchido pelo
     * Spring Security depois de validar a assinatura do token. Requisicao sem
     * token valido nem chega aqui: e barrada antes, com 401.
     *
     * <p>O 403 tambem nao aparece neste arquivo: o servico lanca
     * {@code AluguelNaoPermitidoException}, anotada com
     * {@code @ResponseStatus(FORBIDDEN)}. O controller fica livre de {@code if}s
     * de erro e o status vive junto da regra que o motiva.
     */
    @PostMapping("/godzilla")
    public AluguelResponse alugar(@Valid @RequestBody AlugarFilmeRequest requisicao,
                                  @AuthenticationPrincipal Jwt jwt) {

        Long usuarioId = Long.valueOf(jwt.getSubject());

        return aluguelService.alugar(requisicao.filmeId(), usuarioId);
    }
}
