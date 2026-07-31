package com.godzilla.locadora.controller;

import com.godzilla.locadora.dto.AutenticacaoResponse;
import com.godzilla.locadora.dto.CadastroUsuarioRequest;
import com.godzilla.locadora.dto.LoginRequest;
import com.godzilla.locadora.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Teste 3 do desafio: servico de usuarios.
 *
 * <p>O enunciado grafa o caminho como {@code "usuários/usuário"}. URLs com
 * acento exigem codificacao percentual e sao uma fonte classica de erro entre
 * clientes; adotamos a forma sem acento, {@code /usuarios/usuario}.
 *
 * <p>O texto pede "incluir um novo cliente" e "ao realizar a autenticacao do
 * usuario, deve retornar um Token", mas o exemplo de corpo traz apenas e-mail e
 * senha — que e um payload de login, nao de cadastro. Para atender as duas
 * leituras sem ambiguidade, existem dois endpoints, ambos devolvendo o mesmo
 * formato de resposta com o token.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Cadastra um cliente e ja devolve o token. */
    @PostMapping("/usuario")
    @ResponseStatus(HttpStatus.CREATED)
    public AutenticacaoResponse cadastrar(@Valid @RequestBody CadastroUsuarioRequest requisicao) {
        return usuarioService.cadastrar(requisicao.nome(), requisicao.email(), requisicao.senha());
    }

    /** Autentica um cliente ja cadastrado e devolve o token. */
    @PostMapping("/login")
    public AutenticacaoResponse autenticar(@Valid @RequestBody LoginRequest requisicao) {
        return usuarioService.autenticar(requisicao.email(), requisicao.senha());
    }
}
