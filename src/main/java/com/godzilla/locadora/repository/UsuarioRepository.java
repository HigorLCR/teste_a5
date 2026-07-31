package com.godzilla.locadora.repository;

import com.godzilla.locadora.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso a dados de {@link Usuario}.
 *
 * <p>Os dois metodos abaixo sao <i>derived queries</i>: o Spring Data monta o SQL
 * a partir do nome do metodo, sem nenhuma anotacao. {@code findByEmail} vira
 * {@code select ... from usuario where email = ?}.
 *
 * <p>O e-mail chega aqui ja normalizado para minusculas pela camada de servico,
 * de modo que a comparacao exata e suficiente e aproveita o indice unico.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
