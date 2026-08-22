package milor_backend.repository;

import milor_backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Permite buscar el usuario sin importar si escriben mayúsculas o minúsculas[cite: 11]
    Optional<Usuario> findByUsernameIgnoreCase(String username);
}