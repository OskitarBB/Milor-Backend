package milor_backend.controller;

import lombok.RequiredArgsConstructor;
import milor_backend.dto.DashboardMetricasDTO;
import milor_backend.dto.RegistroVentaRequest;
import milor_backend.entity.VentaRegistro;
import milor_backend.service.VentaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class VentaController {

    private final VentaService ventaService;

    @PostMapping
    public ResponseEntity<VentaRegistro> registrarVenta(@RequestBody RegistroVentaRequest request) {
        return ResponseEntity.ok(ventaService.registrarVenta(request));
    }

    @GetMapping("/metricas")
    public ResponseEntity<DashboardMetricasDTO> obtenerMetricas() {
        return ResponseEntity.ok(ventaService.obtenerMetricas());
    }

    // Endpoint exclusivo para recibir los parámetros del Dashboard Histórico
    @GetMapping("/historico")
    public ResponseEntity<DashboardMetricasDTO> obtenerHistorial(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(ventaService.obtenerMetricasHistorico(inicio, fin));
    }
}