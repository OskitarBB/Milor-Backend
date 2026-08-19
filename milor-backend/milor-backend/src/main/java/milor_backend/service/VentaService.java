package milor_backend.service;

import lombok.RequiredArgsConstructor;
import milor_backend.dto.DashboardMetricasDTO;
import milor_backend.dto.ItemVentaRequest;
import milor_backend.dto.RegistroVentaRequest;
import milor_backend.entity.DetalleVenta;
import milor_backend.entity.Entrada;
import milor_backend.entity.Plato;
import milor_backend.entity.VentaRegistro;
import milor_backend.repository.DetalleVentaRepository;
import milor_backend.repository.EntradaRepository;
import milor_backend.repository.PlatoRepository;
import milor_backend.repository.VentaRegistroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRegistroRepository ventaRegistroRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final PlatoRepository platoRepository;
    private final EntradaRepository entradaRepository;

    @Transactional
    public VentaRegistro registrarVenta(RegistroVentaRequest request) {
        BigDecimal total = BigDecimal.ZERO;

        VentaRegistro venta = VentaRegistro.builder()
                .modalidad(request.getModalidad())
                .fechaHora(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .build();

        VentaRegistro ventaGuardada = ventaRegistroRepository.save(venta);

        for (ItemVentaRequest item : request.getItems()) {
            Plato plato = platoRepository.findById(item.getPlatoId())
                    .orElseThrow(() -> new RuntimeException("Plato no encontrado"));

            if (!Boolean.TRUE.equals(plato.getEsIlimitado())) {
                if (plato.getStock() <= 0) {
                    throw new RuntimeException("No hay stock suficiente para: " + plato.getNombre());
                }
                plato.setStock(plato.getStock() - 1);
                platoRepository.save(plato);
            }

            Entrada entrada = null;
            if (item.getEntradaId() != null) {
                entrada = entradaRepository.findById(item.getEntradaId()).orElse(null);
            }

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(ventaGuardada)
                    .plato(plato)
                    .entrada(entrada)
                    .tipo(item.getTipo())
                    .subtotal(item.getSubtotal())
                    .build();

            detalleVentaRepository.save(detalle);
            total = total.add(item.getSubtotal());
        }

        ventaGuardada.setTotal(total);
        return ventaRegistroRepository.save(ventaGuardada);
    }

    @Transactional(readOnly = true)
    public DashboardMetricasDTO obtenerMetricas() {
        List<VentaRegistro> ventas = ventaRegistroRepository.findAll();
        List<DetalleVenta> detalles = detalleVentaRepository.findAll();
        List<Plato> platos = platoRepository.findAll();

        BigDecimal totalRecaudado = ventas.stream()
                .map(VentaRegistro::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalLocal = (int) ventas.stream()
                .filter(v -> "LOCAL".equalsIgnoreCase(String.valueOf(v.getModalidad())))
                .count();

        int totalLlevar = (int) ventas.stream()
                .filter(v -> "LLEVAR".equalsIgnoreCase(String.valueOf(v.getModalidad())))
                .count();

        int totalConEntrada = (int) detalles.stream()
                .filter(d -> d.getEntrada() != null)
                .count();

        int totalSinEntrada = (int) detalles.stream()
                .filter(d -> d.getEntrada() == null)
                .count();

        int totalMenus = detalles.size();

        Map<Long, DashboardMetricasDTO.DetallePlatoMetrica> conteoPorPlato = new HashMap<>();

        for (Plato plato : platos) {
            int vendidos = (int) detalles.stream()
                    .filter(d -> d.getPlato() != null && d.getPlato().getId().equals(plato.getId()))
                    .count();

            String stockStr = Boolean.TRUE.equals(plato.getEsIlimitado()) ? "Ilimitado" : String.valueOf(plato.getStock());

            conteoPorPlato.put(plato.getId(), DashboardMetricasDTO.DetallePlatoMetrica.builder()
                    .nombre(plato.getNombre())
                    .vendidos(vendidos)
                    .stockRestante(stockStr)
                    .build());
        }

        // Mapeo ordenado de las últimas 10 ventas
        Map<Long, List<DetalleVenta>> detallesPorVenta = detalles.stream()
                .filter(d -> d.getVenta() != null)
                .collect(Collectors.groupingBy(d -> d.getVenta().getId()));

        List<DashboardMetricasDTO.OrdenRecienteDTO> ultimasVentas = ventas.stream()
                .sorted((v1, v2) -> v2.getFechaHora().compareTo(v1.getFechaHora()))
                .limit(10)
                .map(v -> {
                    List<DetalleVenta> itemsVenta = detallesPorVenta.getOrDefault(v.getId(), new ArrayList<>());
                    List<String> descripciones = itemsVenta.stream()
                            .map(d -> {
                                String nomPlato = d.getPlato() != null ? d.getPlato().getNombre() : "Plato";
                                String nomEntrada = d.getEntrada() != null ? " (" + d.getEntrada().getNombre() + ")" : "";
                                return nomPlato + nomEntrada;
                            })
                            .collect(Collectors.toList());

                    return DashboardMetricasDTO.OrdenRecienteDTO.builder()
                            .id(v.getId())
                            .fechaHora(v.getFechaHora())
                            .modalidad(v.getModalidad())
                            .total(v.getTotal())
                            .descripcionItems(descripciones)
                            .build();
                })
                .collect(Collectors.toList());

        return DashboardMetricasDTO.builder()
                .totalRecaudado(totalRecaudado)
                .totalMenusVendidos(totalMenus)
                .totalLocal(totalLocal)
                .totalLlevar(totalLlevar)
                .totalConEntrada(totalConEntrada)
                .totalSinEntrada(totalSinEntrada)
                .conteoPorPlato(conteoPorPlato)
                .ultimasVentas(ultimasVentas)
                .build();
    }
}