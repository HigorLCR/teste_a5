package com.godzilla.locadora.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados da especificacao OpenAPI. Rotas e schemas o springdoc infere
 * sozinho; o esquema {@code bearerAuth} e apenas declarado aqui e aplicado
 * individualmente nos endpoints protegidos, ja que a maioria e publica.
 */
@Configuration
public class OpenApiConfig {

    public static final String ESQUEMA_BEARER = "bearerAuth";

    @Bean
    public OpenAPI locadoraOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Godzilla Local Filmes — API")
                        .version("1.0.0")
                        .description("""
                                API de locadora especializada na saga Gojira, em que cada cliente \
                                pode manter apenas um filme alugado por vez.

                                Para testar o aluguel: cadastre-se ou faca login, copie o valor de \
                                `token` da resposta, clique em **Authorize** e cole o token."""))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtido em /usuarios/usuario ou /usuarios/login")));
    }
}
