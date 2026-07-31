package com.godzilla.locadora.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O vinculo entre um cliente e um filme durante o periodo de locacao.
 *
 * <p>{@code devolvidoEm} nulo significa aluguel EM ABERTO. E sobre essa condicao
 * que o indice unico parcial {@code uk_aluguel_ativo_por_usuario} atua, impedindo
 * que o mesmo cliente tenha dois alugueis em aberto ao mesmo tempo.
 */
@Entity
@Table(name = "aluguel")
@Getter
@Setter
@NoArgsConstructor
public class Aluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY explicito: o padrao de @ManyToOne e EAGER, que carregaria o filme
    // inteiro em toda consulta de aluguel, mesmo quando nao for usado. EAGER em
    // varias relacoes e a origem mais comum de N+1 queries.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "filme_id", nullable = false)
    private Filme filme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "alugado_em", nullable = false)
    private Instant alugadoEm;

    @Column(name = "devolvido_em")
    private Instant devolvidoEm;

    public Aluguel(Filme filme, Usuario usuario) {
        this.filme = filme;
        this.usuario = usuario;
        this.alugadoEm = Instant.now();
    }

    public boolean emAberto() {
        return devolvidoEm == null;
    }
}
