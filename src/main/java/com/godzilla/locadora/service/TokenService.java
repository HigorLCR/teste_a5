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
 * Emissao dos tokens JWT. O payload e apenas Base64, legivel por qualquer um: o
 * token garante integridade, nao sigilo. Nada sensivel entra nas claims.
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
                // O subject e o id do cliente: e dele que o aluguel tira a identidade.
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("nome", usuario.getNome())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
