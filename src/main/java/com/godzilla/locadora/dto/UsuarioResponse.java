package com.godzilla.locadora.dto;

import com.godzilla.locadora.domain.Usuario;

/** Dados publicos do cliente. Nem a senha nem o hash dela aparecem aqui. */
public record UsuarioResponse(Long id, String email, String nome) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getEmail(), usuario.getNome());
    }
}
