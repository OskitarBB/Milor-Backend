package milor_backend.service;

import lombok.RequiredArgsConstructor;
import milor_backend.entity.Plato;
import milor_backend.entity.Turno;
import milor_backend.repository.PlatoRepository;
import milor_backend.repository.TurnoRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final PlatoRepository platoRepository;
    private final CartaService cartaService;
    private final VentaService ventaService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void cerrarTurnoActual() {
        // 1. Cerrar el turno activo
        Turno turnoActual = turnoRepository.findByEstado("ABIERTO")
                .orElseThrow(() -> new RuntimeException("No hay turno abierto. Por favor, crea uno en la base de datos."));

        turnoActual.setFechaCierre(LocalDateTime.now());
        turnoActual.setEstado("CERRADO");
        turnoRepository.save(turnoActual);

        // 2. Inactivar todo el menú del día actual (borrado lógico)
        List<Plato> platos = platoRepository.findAll();
        platos.forEach(p -> p.setActivo(false));
        platoRepository.saveAll(platos);

        // 3. Abrir un nuevo turno en blanco automáticamente
        Turno nuevoTurno = Turno.builder()
                .fechaApertura(LocalDateTime.now())
                .estado("ABIERTO")
                .build();
        turnoRepository.save(nuevoTurno);

        // 4. Refrescar el frontend al instante mediante WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/carta", cartaService.obtenerCartaActual());
            messagingTemplate.convertAndSend("/topic/metricas", ventaService.obtenerMetricas());
        } catch (Exception e) {
            System.err.println("Error al emitir métricas por WebSocket en cierre de turno: " + e.getMessage());
        }
    }

    @Transactional
    public void inicializarPrimerTurno() {
        // Verificamos si ya existe un turno abierto para no duplicar
        boolean existeAbierto = turnoRepository.findByEstado("ABIERTO").isPresent();

        if (!existeAbierto) {
            Turno nuevoTurno = Turno.builder()
                    .fechaApertura(LocalDateTime.now())
                    .estado("ABIERTO")
                    .build();
            turnoRepository.save(nuevoTurno);
            System.out.println("Primer turno inicializado con éxito.");
        }
    }
}