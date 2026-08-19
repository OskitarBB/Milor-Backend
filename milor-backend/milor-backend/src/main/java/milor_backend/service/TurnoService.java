package milor_backend.service;

import lombok.RequiredArgsConstructor;
import milor_backend.entity.Turno;
import milor_backend.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;

    @Transactional
    public Turno abrirNuevoTurno() {
        // Opcional: Cierra cualquier turno abierto previo por seguridad
        turnoRepository.findTopByEstadoOrderByIdDesc("ABIERTO").ifPresent(turno -> {
            turno.setEstado("CERRADO");
            turno.setFechaCierre(LocalDateTime.now());
            turnoRepository.save(turno);
        });

        Turno nuevoTurno = Turno.builder()
                .estado("ABIERTO")
                .fechaApertura(LocalDateTime.now())
                .build();
        return turnoRepository.save(nuevoTurno);
    }

    @Transactional(readOnly = true)
    public Optional<Turno> obtenerTurnoAbierto() {
        return turnoRepository.findTopByEstadoOrderByIdDesc("ABIERTO");
    }

    @Transactional
    public void cerrarTurnoActual() {
        Optional<Turno> turnoOpt = turnoRepository.findTopByEstadoOrderByIdDesc("ABIERTO");
        if (turnoOpt.isPresent()) {
            Turno turno = turnoOpt.get();
            turno.setEstado("CERRADO");
            turno.setFechaCierre(LocalDateTime.now());
            turnoRepository.save(turno);
        }
    }
}