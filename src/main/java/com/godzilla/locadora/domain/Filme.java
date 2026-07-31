package com.godzilla.locadora.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Um titulo do catalogo. {@code estoque} sao as unidades disponiveis agora. */
@Entity
@Table(name = "filme")
@Getter
@Setter
@NoArgsConstructor
public class Filme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 150)
    private String diretor;

    private Short ano;

    @Column(nullable = false)
    private int estoque;

    public Filme(String titulo, String diretor, Short ano, int estoque) {
        this.titulo = titulo;
        this.diretor = diretor;
        this.ano = ano;
        this.estoque = estoque;
    }

    public boolean disponivel() {
        return estoque > 0;
    }
}
