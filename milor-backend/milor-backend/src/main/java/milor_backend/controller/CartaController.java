package milor_backend.controller;

import lombok.RequiredArgsConstructor;
import milor_backend.dto.CartaDiariaDTO;
import milor_backend.entity.ConfiguracionPrecio;
import milor_backend.entity.Entrada;
import milor_backend.entity.Plato;
import milor_backend.service.CartaService;
import milor_backend.service.VentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carta")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class CartaController {

    private final CartaService cartaService;
    private final VentaService ventaService; // Inyectado para calcular las métricas frescas
    private final SimpMessagingTemplate messagingTemplate; // Inyectado para enviar el WebSocket

    @GetMapping
    public ResponseEntity<CartaDiariaDTO> obtenerCarta() {
        return ResponseEntity.ok(cartaService.obtenerCartaActual());
    }

    @PostMapping("/platos")
    public ResponseEntity<Plato> guardarPlato(@RequestBody Plato plato) {
        Plato guardado = cartaService.guardarPlato(plato);
        notificarActualizacionGlobal();
        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/platos/{id}")
    public ResponseEntity<CartaDiariaDTO> eliminarPlato(@PathVariable Long id) {
        cartaService.eliminarPlato(id);
        notificarActualizacionGlobal();
        return ResponseEntity.ok(cartaService.obtenerCartaActual());
    }

    @PostMapping("/entradas")
    public ResponseEntity<Entrada> guardarEntrada(@RequestBody Entrada entrada) {
        Entrada guardada = cartaService.guardarEntrada(entrada);
        notificarActualizacionGlobal();
        return ResponseEntity.ok(guardada);
    }

    @DeleteMapping("/entradas/{id}")
    public ResponseEntity<CartaDiariaDTO> eliminarEntrada(@PathVariable Long id) {
        cartaService.eliminarEntrada(id);
        notificarActualizacionGlobal();
        return ResponseEntity.ok(cartaService.obtenerCartaActual());
    }

    @PutMapping("/precios")
    public ResponseEntity<ConfiguracionPrecio> actualizarPrecios(@RequestBody ConfiguracionPrecio precios) {
        ConfiguracionPrecio actualizado = cartaService.actualizarPrecios(precios);
        notificarActualizacionGlobal();
        return ResponseEntity.ok(actualizado);
    }

    /**
     * ESTE ES EL MOTOR DEL TIEMPO REAL:
     * Cada vez que se toca la carta (editar, inhabilitar, eliminar, cambiar stock),
     * el controlador avisa inmediatamente a todos los navegadores abiertos.
     */
    private void notificarActualizacionGlobal() {
        try {
            // 1. Refresca a quienes estén viendo /admin/carta o /operador
            messagingTemplate.convertAndSend("/topic/carta", cartaService.obtenerCartaActual());

            // 2. Refresca a quienes estén viendo /admin/dashboard
            messagingTemplate.convertAndSend("/topic/metricas", ventaService.obtenerMetricas());
        } catch (Exception e) {
            System.err.println("Error al emitir por WebSocket: " + e.getMessage());
        }
    }
}