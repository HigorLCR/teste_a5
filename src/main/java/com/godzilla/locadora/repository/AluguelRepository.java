package com.godzilla.locadora.repository;

import com.godzilla.locadora.domain.Aluguel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
