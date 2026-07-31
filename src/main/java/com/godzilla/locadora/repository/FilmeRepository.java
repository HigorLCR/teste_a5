package com.godzilla.locadora.repository;

import com.godzilla.locadora.domain.Filme;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * Reserva uma unidade de forma atomica: leitura e escrita no mesmo comando,
     * sob lock de linha. Retorna 1 se reservou, 0 se nao havia estoque — sem
     * janela entre verificar e alterar.
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
