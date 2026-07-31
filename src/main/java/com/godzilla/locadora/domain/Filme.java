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

/**
 * Um titulo do catalogo da locadora.
 *
 * <p>{@code estoque} representa quantas unidades estao disponiveis para aluguel
 * neste momento — nao o total adquirido. Alugar decrementa, devolver incrementa.
 */
@Entity
@Table(name = "filme")
@Getter
@Setter
// O JPA exige um construtor sem argumentos: o Hibernate instancia a entidade por
// reflexao antes de preencher os campos com os dados vindos do banco.
@NoArgsConstructor
// Note que NAO usamos @Data nem @EqualsAndHashCode. Em entidade JPA eles sao uma
// armadilha: geram equals/hashCode sobre todos os campos, incluindo relacoes
// lazy — o que dispara consultas inesperadas e quebra o contrato de hashCode
// assim que um campo muda depois de a entidade entrar em uma coleção.
public class Filme {

    @Id
    // IDENTITY delega a geracao ao banco (coluna GENERATED ... AS IDENTITY).
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
