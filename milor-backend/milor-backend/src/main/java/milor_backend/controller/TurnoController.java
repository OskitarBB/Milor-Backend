package milor_backend.controller;

import lombok.RequiredArgsConstructor;
import milor_backend.service.TurnoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
public class TurnoController {

    private final TurnoService turnoService;

    @PostMapping("/cerrar")
    public ResponseEntity<Void> cerrarTurno() {
        turnoService.cerrarTurnoActual();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inicializar")
    public ResponseEntity<String> inicializarTurno() {
        turnoService.inicializarPrimerTurno();
        return ResponseEntity.ok("Turno inicializado correctamente.");
    }
}