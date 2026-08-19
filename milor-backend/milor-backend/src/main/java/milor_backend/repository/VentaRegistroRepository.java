package milor_backend.repository;

import milor_backend.entity.Turno;
import milor_backend.entity.VentaRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRegistroRepository extends JpaRepository<VentaRegistro, Long> {

    // Filtro para traer los registros históricos de un rango de tiempo
    List<VentaRegistro> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT v FROM VentaRegistro v ORDER BY v.fechaHora DESC")
    List<VentaRegistro> findTop30ByOrderByFechaHoraDesc();

    List<VentaRegistro> findByTurno(Turno turno);
}