package com.godzilla.locadora.dto;

/**
 * Resposta de cadastro e de login, no formato pedido pelo enunciado:
 *
 * <pre>
 * {
 *   "auth": true,
 *   "usuario": { "id": 5, "email": "cliente@cliente.com", "nome": "cliente" },
 *   "token": "eyJhbGciOiJIUzI1NiJ9..."
 * }
 * </pre>
 */
public record AutenticacaoResponse(boolean auth, UsuarioResponse usuario, String token) {

    public static AutenticacaoResponse autenticado(UsuarioResponse usuario, String token) {
        return new AutenticacaoResponse(true, usuario, token);
    }
}
