package milor_backend.repository;

import milor_backend.entity.VentaRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRegistroRepository extends JpaRepository<VentaRegistro, Long> {
    List<VentaRegistro> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT v FROM VentaRegistro v ORDER BY v.fechaHora DESC")
    List<VentaRegistro> findTop30ByOrderByFechaHoraDesc();
}