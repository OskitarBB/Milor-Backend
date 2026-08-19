package milor_backend.repository;

import milor_backend.entity.ConfiguracionPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionPrecioRepository extends JpaRepository<ConfiguracionPrecio, Long> {
}