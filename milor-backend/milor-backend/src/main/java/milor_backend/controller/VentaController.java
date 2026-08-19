package milor_backend.controller;

import lombok.RequiredArgsConstructor;
import milor_backend.dto.DashboardMetricasDTO;
import milor_backend.dto.RegistroVentaRequest;
import milor_backend.entity.VentaRegistro;
import milor_backend.service.VentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class VentaController {

    private final VentaService ventaService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<VentaRegistro> registrarVenta(@RequestBody @Validated RegistroVentaRequest request) {
        VentaRegistro venta = ventaService.registrarVenta(request);
        messagingTemplate.convertAndSend("/topic/metricas", ventaService.obtenerMetricas());
        return ResponseEntity.ok(venta);
    }

    @GetMapping("/metricas")
    public ResponseEntity<DashboardMetricasDTO> obtenerMetricas() {
        return ResponseEntity.ok(ventaService.obtenerMetricas());
    }
}