package milor_backend.controller;

import lombok.RequiredArgsConstructor;
import milor_backend.entity.Turno;
import milor_backend.service.TurnoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class TurnoController {

    private final TurnoService turnoService;

    @PostMapping("/abrir")
    public ResponseEntity<Turno> abrirTurno() {
        // Llamamos al método oficial de tu servicio para abrir turno
        return ResponseEntity.ok(turnoService.abrirNuevoTurno());
    }

    @PostMapping("/cerrar")
    public ResponseEntity<Void> cerrarTurno() {
        turnoService.cerrarTurnoActual();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/estado-actual")
    public ResponseEntity<Turno> obtenerTurnoActual() {
        // Usamos el método que provee el servicio para obtener el turno abierto
        Optional<Turno> turno = turnoService.obtenerTurnoAbierto();
        return turno.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}