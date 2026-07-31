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

    /** Cadastra um cliente e ja devolve o token, dispensando um login em seguida. */
    @Transactional
    public AutenticacaoResponse cadastrar(String nome, String email, String senha) {
        String emailNormalizado = normalizar(email);

        // Mensagem clara no caso comum; a garantia e a constraint, tratada abaixo.
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException(
                    "Ja existe um cliente cadastrado com o e-mail %s.".formatted(emailNormalizado));
        }

        Usuario usuario = new Usuario(
                nome.trim(), emailNormalizado, passwordEncoder.encode(senha));

        try {
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new EmailJaCadastradoException(
                    "Ja existe um cliente cadastrado com o e-mail %s.".formatted(emailNormalizado));
        }

        return AutenticacaoResponse.autenticado(
                UsuarioResponse.de(usuario), tokenService.gerarPara(usuario));
    }

    @Transactional(readOnly = true)
    public AutenticacaoResponse autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(normalizar(email))
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        return AutenticacaoResponse.autenticado(
                UsuarioResponse.de(usuario), tokenService.gerarPara(usuario));
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
