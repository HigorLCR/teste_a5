package com.godzilla.locadora.repository;

import com.godzilla.locadora.domain.Filme;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acesso a dados de {@link Filme}.
 *
 * <p>Repare que esta e uma <b>interface sem implementacao</b>: o Spring Data gera
 * a classe concreta em tempo de execucao. Herdar de {@link JpaRepository} ja
 * fornece save, findById, findAll, delete e companhia.
 */
public interface FilmeRepository extends JpaRepository<Filme, Long> {

    @Query("""
           SELECT f
             FROM Filme f
            WHERE LOWER(f.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))
            ORDER BY f.ano, f.titulo
           """)
    List<Filme> buscarPorTitulo(@Param("titulo") String titulo);

    @Query("""
           SELECT f
             FROM Filme f
            WHERE LOWER(f.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))
              AND f.ano = :ano
            ORDER BY f.ano, f.titulo
           """)
    List<Filme> buscarPorTituloEAno(@Param("titulo") String titulo, @Param("ano") Short ano);

    /**
     * Reserva uma unidade do filme, de forma atomica.
     *
     * <p>Este e o ponto mais importante do desafio do lado do estoque. A forma
     * ingenua seria:
     *
     * <pre>
     *   Filme f = repo.findById(id);
     *   if (f.getEstoque() &gt; 0) {      // (A)
     *       f.setEstoque(f.getEstoque() - 1);
     *       repo.save(f);                // (B)
     *   }
     * </pre>
     *
     * <p>Sob concorrencia isso falha: duas requisicoes podem passar por (A)
     * antes de qualquer uma chegar em (B), e o estoque acaba negativo — ou dois
     * clientes recebem a ultima copia.
     *
     * <p>Aqui a leitura e a escrita acontecem em um unico comando SQL, avaliado
     * pelo banco sob o lock da linha. Ou o decremento acontece, ou nao acontece.
     *
     * @return 1 se havia estoque e a unidade foi reservada; 0 se nao havia.
     *         Nao ha terceiro resultado, e nao ha janela entre checar e alterar.
     */
    @Modifying
    @Query("""
           UPDATE Filme f
              SET f.estoque = f.estoque - 1
            WHERE f.id = :id
              AND f.estoque > 0
           """)
    int reservarUmaUnidade(@Param("id") Long id);

    /**
     * Contrapartida de {@link #reservarUmaUnidade(Long)}. Somar no banco, e nao
     * ler-calcular-gravar na aplicacao, evita lost update entre concorrentes.
     */
    @Modifying
    @Query("""
           UPDATE Filme f
              SET f.estoque = f.estoque + 1
            WHERE f.id = :id
           """)
    int devolverUmaUnidade(@Param("id") Long id);
}
