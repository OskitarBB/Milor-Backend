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

    // 1. Método para abrir un nuevo turno
    @Transactional
    public Turno abrirNuevoTurno() {
        Turno nuevoTurno = Turno.builder()
                .estado("ABIERTO")
                .fechaApertura(LocalDateTime.now())
                .build();
        return turnoRepository.save(nuevoTurno);
    }

    // 2. Método para obtener el turno abierto actual de forma segura
    @Transactional(readOnly = true)
    public Optional<Turno> obtenerTurnoAbierto() {
        return turnoRepository.findTopByEstadoOrderByIdDesc("ABIERTO");
    }

    // 3. Tu método existente para cerrar turno (mantenlo tal cual lo tengas)
    @Transactional
    public void cerrarTurnoActual() {
        Optional<Turno> turnoOpt = turnoRepository.findTopByEstadoOrderByIdDesc("ABIERTO");
        if (turnoOpt.isPresent()) {
            Turno turno = turnoOpt.get();
            turno.setEstado("CERRADO");
            turno.setFechaCierre(LocalDateTime.now());
            turnoRepository.save(turno);
        }

        // Opcional: abre uno nuevo automáticamente al cerrar, o déjalo vacío según prefieras.
        // abrirNuevoTurno();
    }
}