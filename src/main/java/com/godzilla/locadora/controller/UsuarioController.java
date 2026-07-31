package com.godzilla.locadora.controller;

import com.godzilla.locadora.dto.AutenticacaoResponse;
import com.godzilla.locadora.dto.CadastroUsuarioRequest;
import com.godzilla.locadora.dto.LoginRequest;
import com.godzilla.locadora.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
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
 * <p>O enunciado pede cadastro, mas o exemplo de corpo traz apenas e-mail e
 * senha, que e um payload de login. Os dois endpoints existem, com o mesmo
 * formato de resposta.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Cadastra um novo cliente",
            description = "Cria o cliente e ja devolve o token JWT, dispensando um login em seguida. "
                    + "Retorna 201; e-mail ja cadastrado retorna 409 e campos invalidos, 400.")
    @PostMapping("/usuario")
    @ResponseStatus(HttpStatus.CREATED)
    public AutenticacaoResponse cadastrar(@Valid @RequestBody CadastroUsuarioRequest requisicao) {
        return usuarioService.cadastrar(requisicao.nome(), requisicao.email(), requisicao.senha());
    }

    @Operation(summary = "Autentica um cliente ja cadastrado",
            description = "Confere e-mail e senha e devolve o token JWT usado nos endpoints "
                    + "protegidos. Credenciais incorretas retornam 401.")
    @PostMapping("/login")
    public AutenticacaoResponse autenticar(@Valid @RequestBody LoginRequest requisicao) {
        return usuarioService.autenticar(requisicao.email(), requisicao.senha());
    }
}
