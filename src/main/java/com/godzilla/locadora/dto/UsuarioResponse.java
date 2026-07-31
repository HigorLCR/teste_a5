package com.godzilla.locadora.dto;

import com.godzilla.locadora.domain.Usuario;

/**
 * Dados publicos do cliente, no formato do exemplo do enunciado.
 *
 * <p>Note o que NAO esta aqui: nem a senha, nem o hash dela. Um DTO separado da
 * entidade e o que torna esse tipo de vazamento impossivel por construcao, e nao
 * por lembrança do programador.
 */
public record UsuarioResponse(Long id, String email, String nome) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getEmail(), usuario.getNome());
    }
}
