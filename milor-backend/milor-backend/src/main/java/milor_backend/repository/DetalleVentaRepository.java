package milor_backend.repository;

import milor_backend.entity.DetalleVenta;
import milor_backend.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    boolean existsByPlatoId(Long platoId);
    boolean existsByEntradaId(Long entradaId);

    List<DetalleVenta> findByVentaTurno(Turno turno);

    // Filtro estricto por rango de fechas (navegando hacia la fecha de la venta)
    List<DetalleVenta> findByVenta_FechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
}