package milor_backend.service;

import lombok.RequiredArgsConstructor;
import milor_backend.entity.Turno;
import milor_backend.repository.PlatoRepository;
import milor_backend.repository.EntradaRepository;
import milor_backend.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final PlatoRepository platoRepository;
    private final EntradaRepository entradaRepository;

    @Transactional
    public Turno abrirNuevoTurno() {
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

            // 🚀 Al cerrar turno, inactivamos visualmente todos los platos y entradas
            // para que la carta aparezca vacía al iniciar el siguiente turno, protegiendo el historial.
            platoRepository.findAll().forEach(p -> {
                p.setActivo(false);
                platoRepository.save(p);
            });
            entradaRepository.findAll().forEach(e -> {
                e.setActivo(false);
                entradaRepository.save(e);
            });
        }
    }
}