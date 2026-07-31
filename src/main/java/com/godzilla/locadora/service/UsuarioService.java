package com.godzilla.locadora.service;

import com.godzilla.locadora.domain.Usuario;
import com.godzilla.locadora.dto.AutenticacaoResponse;
import com.godzilla.locadora.dto.UsuarioResponse;
import com.godzilla.locadora.exception.CredenciaisInvalidasException;
import com.godzilla.locadora.exception.EmailJaCadastradoException;
import com.godzilla.locadora.repository.UsuarioRepository;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro e autenticacao de clientes.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * Cadastra um novo cliente e ja devolve o token, evitando obrigar um login
     * logo em seguida.
     */
    @Transactional
    public AutenticacaoResponse cadastrar(String nome, String email, String senha) {
        String emailNormalizado = normalizar(email);

        // Verificacao antecipada apenas para a mensagem de erro ser clara. A
        // garantia real e a constraint uk_usuario_email, tratada no catch abaixo:
        // entre este if e o insert existe uma janela de concorrencia.
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException(
                    "Ja existe um cliente cadastrado com o e-mail %s.".formatted(emailNormalizado));
        }

        Usuario usuario = new Usuario(
                nome.trim(),
                emailNormalizado,
                // A senha e transformada em hash AQUI, na fronteira de entrada.
                // Em nenhum ponto do sistema ela e persistida ou registrada em log.
                passwordEncoder.encode(senha));

        try {
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new EmailJaCadastradoException(
                    "Ja existe um cliente cadastrado com o e-mail %s.".formatted(emailNormalizado));
        }

        return AutenticacaoResponse.autenticado(
                UsuarioResponse.de(usuario), tokenService.gerarPara(usuario));
    }

    /**
     * Autentica o cliente e devolve o token a ser usado nas proximas requisicoes.
     */
    @Transactional(readOnly = true)
    public AutenticacaoResponse autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(normalizar(email))
                .orElseThrow(CredenciaisInvalidasException::new);

        // matches() re-executa o BCrypt sobre a senha informada usando o salt
        // gravado no proprio hash e compara os resultados. Nunca se descriptografa
        // nada — hash e via unica.
        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        return AutenticacaoResponse.autenticado(
                UsuarioResponse.de(usuario), tokenService.gerarPara(usuario));
    }

    /**
     * E-mail nao diferencia maiusculas de minusculas. Normalizar na entrada faz
     * com que "Cliente@Teste.com" e "cliente@teste.com" sejam o mesmo cadastro,
     * e permite que a unicidade no banco seja uma constraint simples.
     */
    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
