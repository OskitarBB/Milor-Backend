package milor_backend.controller;

import lombok.RequiredArgsConstructor;
import milor_backend.entity.Turno;
import milor_backend.service.CartaService;
import milor_backend.service.TurnoService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class TurnoController {

    private final TurnoService turnoService;
    private final CartaService cartaService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/abrir")
    public ResponseEntity<Turno> abrirTurno() {
        Turno turno = turnoService.abrirNuevoTurno();
        notificarEstadoTurno(turno);
        return ResponseEntity.ok(turno);
    }

    @PostMapping("/cerrar")
    public ResponseEntity<Void> cerrarTurno() {
        turnoService.cerrarTurnoActual();
        notificarEstadoTurno(null);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/estado-actual")
    public ResponseEntity<Turno> obtenerTurnoActual() {
        Optional<Turno> turno = turnoService.obtenerTurnoAbierto();
        return turno.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    private void notificarEstadoTurno(Turno turno) {
        try {
            Object payloadTurno = turno != null ? turno : Map.of("estado", "CERRADO");
            messagingTemplate.convertAndSend("/topic/turno", payloadTurno);
            messagingTemplate.convertAndSend("/topic/carta", cartaService.obtenerCartaActual());
        } catch (Exception e) {
            System.err.println("Error al emitir eventos de turno por WebSocket: " + e.getMessage());
        }
    }
}