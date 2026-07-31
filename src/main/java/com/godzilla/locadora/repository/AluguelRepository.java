package com.godzilla.locadora.repository;

import com.godzilla.locadora.domain.Aluguel;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acesso a dados de {@link Aluguel}.
 */
public interface AluguelRepository extends JpaRepository<Aluguel, Long> {

    /**
     * Indica se o cliente ja possui um aluguel em aberto.
     *
     * <p>Serve para responder com uma mensagem clara no caso comum — e nao como
     * garantia da regra. A garantia e o indice unico parcial
     * {@code uk_aluguel_ativo_por_usuario}, no banco: entre esta verificacao e o
     * INSERT existe uma janela em que outra requisicao do mesmo cliente pode se
     * inserir. O servico trata as duas situacoes.
     */
    @Query("""
            select case when count(a) > 0 then true else false end
            from Aluguel a
            where a.usuario.id = :usuarioId
              and a.devolvidoEm is null
            """)
    boolean existsByUsuarioIdAndDevolvidoEmIsNull(Long usuarioId);

    /** {@code join fetch} do filme: o comprovante de devolucao le o titulo. */
    @Query("""
            select a
              from Aluguel a
              join fetch a.filme
             where a.usuario.id = :usuarioId
               and a.devolvidoEm is null
            """)
    Optional<Aluguel> buscarEmAbertoDoUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Fecha o aluguel. A condicao {@code devolvidoEm is null} dentro do UPDATE
     * garante que duas devolucoes simultaneas nao creditem estoque em dobro.
     *
     * @return 1 se este chamador fechou o aluguel; 0 se ja estava fechado.
     */
    @Modifying
    @Query("""
            update Aluguel a
               set a.devolvidoEm = :devolvidoEm
             where a.id = :id
               and a.devolvidoEm is null
            """)
    int registrarDevolucao(@Param("id") Long id, @Param("devolvidoEm") Instant devolvidoEm);
}
