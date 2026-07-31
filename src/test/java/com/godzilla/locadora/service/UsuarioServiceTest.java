package com.godzilla.locadora.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.godzilla.locadora.domain.Usuario;
import com.godzilla.locadora.dto.AutenticacaoResponse;
import com.godzilla.locadora.exception.CredenciaisInvalidasException;
import com.godzilla.locadora.exception.EmailJaCadastradoException;
import com.godzilla.locadora.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private TokenService tokenService;
    private UsuarioService usuarioService;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenService = mock(TokenService.class);
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, tokenService);
    }

    @Test
    @DisplayName("cadastra normalizando o e-mail e gravando a senha em hash")
    void deveCadastrarNormalizandoEmailEGravandoHash() {
        when(usuarioRepository.existsByEmail("cliente@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha-secreta")).thenReturn("$2a$10$hash");
        when(tokenService.gerarPara(any(Usuario.class))).thenReturn("token-jwt");

        AutenticacaoResponse resposta =
                usuarioService.cadastrar("  Cliente  ", "  CLIENTE@Teste.COM  ", "senha-secreta");

        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(capturado.capture());

        assertThat(capturado.getValue().getEmail()).isEqualTo("cliente@teste.com");
        assertThat(capturado.getValue().getNome()).isEqualTo("Cliente");
        assertThat(capturado.getValue().getSenhaHash()).isEqualTo("$2a$10$hash");
        assertThat(capturado.getValue().getSenhaHash()).isNotEqualTo("senha-secreta");

        assertThat(resposta.auth()).isTrue();
        assertThat(resposta.token()).isEqualTo("token-jwt");
    }

    @Test
    @DisplayName("recusa cadastro com e-mail ja existente")
    void deveRecusarEmailDuplicado() {
        when(usuarioRepository.existsByEmail("cliente@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar("Cliente", "Cliente@Teste.com", "1234"))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("autentica e devolve token quando as credenciais conferem")
    void deveAutenticarComCredenciaisValidas() {
        Usuario usuario = new Usuario("Cliente", "cliente@teste.com", "$2a$10$hash");
        usuario.setId(7L);

        when(usuarioRepository.findByEmail("cliente@teste.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-secreta", "$2a$10$hash")).thenReturn(true);
        when(tokenService.gerarPara(usuario)).thenReturn("token-jwt");

        AutenticacaoResponse resposta = usuarioService.autenticar("cliente@teste.com", "senha-secreta");

        assertThat(resposta.auth()).isTrue();
        assertThat(resposta.token()).isEqualTo("token-jwt");
        assertThat(resposta.usuario().id()).isEqualTo(7L);
        assertThat(resposta.usuario().email()).isEqualTo("cliente@teste.com");
    }

    @Test
    @DisplayName("recusa senha incorreta sem gerar token")
    void deveRecusarSenhaIncorreta() {
        Usuario usuario = new Usuario("Cliente", "cliente@teste.com", "$2a$10$hash");
        when(usuarioRepository.findByEmail("cliente@teste.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "$2a$10$hash")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.autenticar("cliente@teste.com", "errada"))
                .isInstanceOf(CredenciaisInvalidasException.class);

        verify(tokenService, never()).gerarPara(any());
    }

    @Test
    @DisplayName("e-mail inexistente falha igual a senha errada, sem revelar o motivo")
    void deveRecusarEmailInexistenteComMensagemGenerica() {
        when(usuarioRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.autenticar("naoexiste@teste.com", "1234"))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessageContaining("E-mail ou senha inválidos");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
