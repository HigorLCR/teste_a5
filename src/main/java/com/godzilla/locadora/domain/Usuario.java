package com.godzilla.locadora.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cliente da locadora.
 *
 * <p>O campo {@code senhaHash} guarda o resultado do BCrypt, nunca a senha
 * digitada. A entidade nao expoe nenhum caminho para recuperar a senha original
 * — isso e proposital, e e o comportamento correto: nem o sistema deve conseguir
 * ler a senha de um usuario.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    /** Sempre normalizado para minusculas pela camada de servico. */
    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    public Usuario(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.criadoEm = Instant.now();
    }
}
