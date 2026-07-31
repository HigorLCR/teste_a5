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
 * Vinculo entre cliente e filme durante a locacao. {@code devolvidoEm} nulo
 * significa aluguel em aberto — condicao do indice unico parcial que impede dois
 * alugueis simultaneos do mesmo cliente.
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
