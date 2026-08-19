package milor_backend.controller;

import lombok.RequiredArgsConstructor;
import milor_backend.dto.CartaDiariaDTO;
import milor_backend.entity.ConfiguracionPrecio;
import milor_backend.entity.Entrada;
import milor_backend.entity.Plato;
import milor_backend.service.CartaService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carta")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class CartaController {

    private final CartaService cartaService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<CartaDiariaDTO> obtenerCarta() {
        return ResponseEntity.ok(cartaService.obtenerCartaActual());
    }

    @PostMapping("/platos")
    public ResponseEntity<Plato> guardarPlato(@RequestBody Plato plato) {
        Plato guardado = cartaService.guardarPlato(plato);
        notificarActualizacionCarta();
        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/platos/{id}")
    public ResponseEntity<Void> eliminarPlato(@PathVariable Long id) {
        cartaService.eliminarPlato(id);
        notificarActualizacionCarta();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/entradas")
    public ResponseEntity<Entrada> guardarEntrada(@RequestBody Entrada entrada) {
        Entrada guardada = cartaService.guardarEntrada(entrada);
        notificarActualizacionCarta();
        return ResponseEntity.ok(guardada);
    }

    @DeleteMapping("/entradas/{id}")
    public ResponseEntity<Void> eliminarEntrada(@PathVariable Long id) {
        cartaService.eliminarEntrada(id);
        notificarActualizacionCarta();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/precios")
    public ResponseEntity<ConfiguracionPrecio> actualizarPrecios(@RequestBody ConfiguracionPrecio precios) {
        ConfiguracionPrecio actualizado = cartaService.actualizarPrecios(precios);
        notificarActualizacionCarta();
        return ResponseEntity.ok(actualizado);
    }

    private void notificarActualizacionCarta() {
        messagingTemplate.convertAndSend("/topic/carta", cartaService.obtenerCartaActual());
    }
}