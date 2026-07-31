package com.godzilla.locadora.repository;

import com.godzilla.locadora.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** O e-mail chega aqui ja normalizado para minusculas pela camada de servico. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
