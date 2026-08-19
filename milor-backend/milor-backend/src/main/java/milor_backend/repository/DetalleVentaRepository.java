package milor_backend.repository;

import milor_backend.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    boolean existsByPlatoId(Long platoId);
    boolean existsByEntradaId(Long entradaId);
}