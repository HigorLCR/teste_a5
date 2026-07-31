package com.godzilla.locadora.service;

import com.godzilla.locadora.domain.Usuario;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Emissao dos tokens JWT.
 *
 * <p>Um JWT tem tres partes separadas por ponto: cabecalho, payload e
 * assinatura. As duas primeiras sao apenas Base64 — <b>legiveis por qualquer
 * um</b>. O que o token garante nao e sigilo, e sim integridade: sem a chave
 * secreta ninguem consegue produzir uma assinatura valida para um payload
 * alterado. Por isso nunca se coloca senha ou dado sensivel dentro dele.
 */
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long duracaoMinutos;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${locadora.jwt.duracao-minutos}") long duracaoMinutos) {
        this.jwtEncoder = jwtEncoder;
        this.duracaoMinutos = duracaoMinutos;
    }

    public String gerarPara(Usuario usuario) {
        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("locadora-godzilla")
                .issuedAt(agora)
                .expiresAt(agora.plus(duracaoMinutos, ChronoUnit.MINUTES))
                // O "subject" e a identidade do dono do token. Guardamos o id do
                // usuario: e ele que o endpoint de aluguel usa para saber QUEM
                // esta alugando, sem confiar em nada que venha no corpo.
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("nome", usuario.getNome())
                .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }
}
