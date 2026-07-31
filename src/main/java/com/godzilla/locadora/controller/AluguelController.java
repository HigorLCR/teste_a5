package com.godzilla.locadora.controller;

import com.godzilla.locadora.config.OpenApiConfig;
import com.godzilla.locadora.dto.AlugarFilmeRequest;
import com.godzilla.locadora.dto.AluguelResponse;
import com.godzilla.locadora.dto.DevolucaoResponse;
import com.godzilla.locadora.service.AluguelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Teste 1 do desafio: aluguel de filmes. */
@RestController
public class AluguelController {

    private final AluguelService aluguelService;

    public AluguelController(AluguelService aluguelService) {
        this.aluguelService = aluguelService;
    }

    /**
     * Aluga um filme para o cliente autenticado: 200 quando permitido, 403 caso
     * contrario. A identidade vem do subject do JWT, nunca do corpo.
     */
    @Operation(summary = "Aluga um filme",
            description = "Registra o aluguel para o cliente do token, desde que haja estoque e ele "
                    + "nao esteja com outro filme — a locadora permite um por vez. Recusa com 403.")
    @SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
    @PostMapping("/godzilla")
    public AluguelResponse alugar(@Valid @RequestBody AlugarFilmeRequest requisicao,
                                  @AuthenticationPrincipal Jwt jwt) {

        return aluguelService.alugar(requisicao.filmeId(), Long.valueOf(jwt.getSubject()));
    }

    /**
     * Devolve o filme que o cliente autenticado esta com. Sem corpo e sem id: o
     * cliente tem no maximo um aluguel em aberto e sua identidade vem do JWT.
     *
     * <p>POST, e nao DELETE: a devolucao encerra o aluguel preenchendo
     * {@code devolvidoEm}, sem apagar o historico.
     */
    @Operation(summary = "Devolve o filme alugado",
            description = "Encerra o aluguel em aberto do cliente do token e repoe a unidade no "
                    + "estoque, liberando-o para alugar de novo. Sem corpo: o cliente tem no maximo "
                    + "um aluguel. Sem filme em maos, 404.")
    @SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
    @PostMapping("/godzilla/devolucao")
    public DevolucaoResponse devolver(@AuthenticationPrincipal Jwt jwt) {

        Long usuarioId = Long.valueOf(jwt.getSubject());

        return aluguelService.devolver(usuarioId);
    }
}
