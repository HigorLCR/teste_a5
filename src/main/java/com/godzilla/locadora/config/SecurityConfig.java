package com.godzilla.locadora.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.DispatcherType;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracao de seguranca da API.
 *
 * <p>Estrategia: JWT assinado com HMAC-SHA256 (algoritmo simetrico — a mesma
 * chave assina e verifica). Para uma API unica que emite e consome os proprios
 * tokens, e a escolha certa; chaves assimetricas (RSA) so passam a valer a pena
 * quando terceiros precisam validar o token sem poder emiti-lo.
 *
 * <p>Nao ha nenhum filtro escrito a mao aqui. O {@code oauth2ResourceServer} do
 * proprio Spring Security ja faz o trabalho: le o cabecalho
 * {@code Authorization: Bearer ...}, valida assinatura e expiracao, e popula o
 * contexto de seguranca. Escrever esse filtro na mao seria mais codigo e mais
 * superficie para errar.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF protege formularios com sessao em navegador. Uma API
                // stateless autenticada por token nao tem o que ser forjado por
                // esse vetor: o navegador nao anexa o Bearer automaticamente.
                .csrf(csrf -> csrf.disable())

                // Sem sessao no servidor. Cada requisicao se prova sozinha pelo
                // token — o que permite escalar horizontalmente sem sessao
                // compartilhada entre instancias.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Quando uma excecao sobe de um controller, o Spring faz
                        // um FORWARD interno para /error. A partir do Spring
                        // Security 6 esse despacho tambem passa pelas regras de
                        // autorizacao — e, sem esta linha, uma requisicao NAO
                        // autenticada que falhasse (por exemplo, cadastro com
                        // e-mail duplicado) teria o forward barrado e receberia
                        // 401 com corpo vazio, escondendo o 409 real.
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()

                        // Cadastro e login precisam ser publicos: sao eles que
                        // entregam o token.
                        .requestMatchers(HttpMethod.POST, "/usuarios/usuario", "/usuarios/login").permitAll()

                        // Consultar o catalogo e publico, como em qualquer
                        // locadora — nao se exige cadastro para ver a prateleira.
                        .requestMatchers(HttpMethod.GET, "/locadora/godzilla", "/localdora/godzilla").permitAll()

                        .requestMatchers("/actuator/health").permitAll()

                        // Todo o resto — inclusive POST /godzilla, o aluguel —
                        // exige token valido.
                        .anyRequest().authenticated())

                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .build();
    }

    /**
     * BCrypt: funcao de hash deliberadamente LENTA e com salt aleatorio embutido.
     *
     * <p>Lentidao aqui e uma virtude: encarece o ataque de forca bruta. O salt
     * automatico faz com que duas pessoas com a mesma senha tenham hashes
     * diferentes, inutilizando rainbow tables.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey chaveJwt(@Value("${locadora.jwt.segredo}") String segredo) {
        return new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey chaveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveJwt));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey chaveJwt) {
        return NimbusJwtDecoder.withSecretKey(chaveJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
