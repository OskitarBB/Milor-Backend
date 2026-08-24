package milor_backend.service;

import lombok.RequiredArgsConstructor;
import milor_backend.dto.DashboardMetricasDTO;
import milor_backend.dto.ItemVentaRequest;
import milor_backend.dto.RegistroVentaRequest;
import milor_backend.entity.*;
import milor_backend.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRegistroRepository ventaRegistroRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final PlatoRepository platoRepository;
    private final EntradaRepository entradaRepository;
    private final TurnoRepository turnoRepository;

    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public VentaRegistro registrarVenta(RegistroVentaRequest request) {
        Turno turnoActual = turnoRepository.findTopByEstadoOrderByIdDesc("ABIERTO")
                .orElseThrow(() -> new RuntimeException("Acción bloqueada: No hay un turno abierto para registrar ventas."));

        BigDecimal total = BigDecimal.ZERO;

        VentaRegistro venta = VentaRegistro.builder()
                .modalidad(request.getModalidad())
                .fechaHora(LocalDateTime.now(ZoneId.of("America/Lima")))
                .total(BigDecimal.ZERO)
                .turno(turnoActual)
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
                    .modalidad(item.getModalidad() != null ? item.getModalidad() : request.getModalidad())
                    .subtotal(item.getSubtotal())
                    .build();

            detalleVentaRepository.save(detalle);
            total = total.add(item.getSubtotal());
        }

        ventaGuardada.setTotal(total);
        VentaRegistro resultadoFinal = ventaRegistroRepository.save(ventaGuardada);

        try {
            messagingTemplate.convertAndSend("/topic/metricas", obtenerMetricas());
            messagingTemplate.convertAndSend("/topic/ventas", resultadoFinal);
            eventPublisher.publishEvent(new VentaRegistradaEvent(this));
        } catch (Exception e) {
            System.err.println("Error al emitir WebSockets tras venta: " + e.getMessage());
        }

        return resultadoFinal;
    }

    @Transactional(readOnly = true)
    public DashboardMetricasDTO obtenerMetricas() {
        Optional<Turno> turnoActivoOpt = turnoRepository.findTopByEstadoOrderByIdDesc("ABIERTO");
        if (turnoActivoOpt.isEmpty()) {
            return DashboardMetricasDTO.builder()
                    .totalRecaudado(BigDecimal.ZERO)
                    .totalMenusVendidos(0)
                    .totalLocal(0)
                    .totalLlevar(0)
                    .totalConEntrada(0)
                    .totalSinEntrada(0)
                    .conteoPorPlato(new HashMap<>())
                    .ultimasVentas(new ArrayList<>())
                    .build();
        }

        Turno turnoActual = turnoActivoOpt.get();
        List<VentaRegistro> ventas = ventaRegistroRepository.findByTurno(turnoActual);
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaTurno(turnoActual);
        List<Plato> platos = platoRepository.findAll();

        return construirMetricas(ventas, detalles, platos);
    }

    @Transactional(readOnly = true)
    public DashboardMetricasDTO obtenerMetricasHistorico(LocalDateTime inicio, LocalDateTime fin) {
        List<VentaRegistro> ventas = ventaRegistroRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin);
        List<DetalleVenta> detalles = detalleVentaRepository.findByVenta_FechaHoraBetween(inicio, fin);
        List<Plato> platos = platoRepository.findAll();

        return construirMetricas(ventas, detalles, platos);
    }

    private DashboardMetricasDTO construirMetricas(List<VentaRegistro> ventas, List<DetalleVenta> detalles, List<Plato> platos) {
        BigDecimal totalRecaudado = ventas.stream()
                .map(VentaRegistro::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalLocal = (int) detalles.stream()
                .filter(d -> "LOCAL".equalsIgnoreCase(String.valueOf(d.getModalidad())))
                .count();

        int totalLlevar = (int) detalles.stream()
                .filter(d -> "LLEVAR".equalsIgnoreCase(String.valueOf(d.getModalidad())))
                .count();

        int totalConEntrada = (int) detalles.stream()
                .filter(d -> d.getEntrada() != null)
                .count();

        int totalSinEntrada = (int) detalles.stream()
                .filter(d -> d.getEntrada() == null)
                .count();

        int totalMenus = detalles.size();

        Map<Long, DashboardMetricasDTO.DetallePlatoMetrica> conteoPorPlato = new HashMap<>();

        // 🚀 Se incluyen todos los platos para que nunca desaparezcan del dashboard, sin importar su estado activo/inactivo
        for (Plato plato : platos) {
            int vendidos = (int) detalles.stream()
                    .filter(d -> d.getPlato() != null && d.getPlato().getId().equals(plato.getId()))
                    .count();

            String stockStr = Boolean.TRUE.equals(plato.getEsIlimitado()) ? "Ilimitado" : String.valueOf(plato.getStock());
            boolean esActivo = plato.getActivo() == null || plato.getActivo();

            conteoPorPlato.put(plato.getId(), DashboardMetricasDTO.DetallePlatoMetrica.builder()
                    .nombre(plato.getNombre())
                    .vendidos(vendidos)
                    .stockRestante(stockStr)
                    .activo(esActivo)
                    .build());
        }

        Map<Long, List<DetalleVenta>> detallesPorVenta = detalles.stream()
                .filter(d -> d.getVenta() != null)
                .collect(Collectors.groupingBy(d -> d.getVenta().getId()));

        List<DashboardMetricasDTO.OrdenRecienteDTO> ultimasVentas = ventas.stream()
                .sorted((v1, v2) -> v2.getFechaHora().compareTo(v1.getFechaHora()))
                .limit(10)
                .map(v -> {
                    List<DetalleVenta> itemsVenta = detallesPorVenta.getOrDefault(v.getId(), new ArrayList<>());

                    Map<String, List<DetalleVenta>> grupo = itemsVenta.stream()
                            .collect(Collectors.groupingBy(d -> {
                                String nomPlato = d.getPlato() != null ? d.getPlato().getNombre() : "Plato";
                                String idEntrada = d.getEntrada() != null ? String.valueOf(d.getEntrada().getId()) : "0";
                                String mod = d.getModalidad() != null ? d.getModalidad().name() : "LOCAL";
                                return nomPlato + "|" + idEntrada + "|" + mod;
                            }));

                    List<DashboardMetricasDTO.ItemOrdenDTO> listaItems = new ArrayList<>();
                    for (List<DetalleVenta> listaDetalles : grupo.values()) {
                        DetalleVenta primero = listaDetalles.get(0);
                        String nomPlato = primero.getPlato() != null ? primero.getPlato().getNombre() : "Plato";
                        String nomEntrada = primero.getEntrada() != null ? primero.getEntrada().getNombre() : null;
                        ModalidadConsumo mod = primero.getModalidad();
                        long cantidad = listaDetalles.size();

                        listaItems.add(DashboardMetricasDTO.ItemOrdenDTO.builder()
                                .platoNombre(nomPlato)
                                .entradaNombre(nomEntrada)
                                .modalidad(mod)
                                .cantidad(cantidad)
                                .build());
                    }

                    return DashboardMetricasDTO.OrdenRecienteDTO.builder()
                            .id(v.getId())
                            .fechaHora(v.getFechaHora())
                            .modalidad(v.getModalidad())
                            .total(v.getTotal())
                            .items(listaItems)
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
