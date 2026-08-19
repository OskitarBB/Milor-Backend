package milor_backend.service;

import lombok.RequiredArgsConstructor;
import milor_backend.dto.CartaDiariaDTO;
import milor_backend.entity.ConfiguracionPrecio;
import milor_backend.entity.Entrada;
import milor_backend.entity.Plato;
import milor_backend.repository.ConfiguracionPrecioRepository;
import milor_backend.repository.DetalleVentaRepository;
import milor_backend.repository.EntradaRepository;
import milor_backend.repository.PlatoRepository;
import milor_backend.repository.TurnoRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartaService {

    private final PlatoRepository platoRepository;
    private final EntradaRepository entradaRepository;
    private final ConfiguracionPrecioRepository configuracionPrecioRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final TurnoRepository turnoRepository;
    private final VentaService ventaService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Transactional
    public CartaDiariaDTO obtenerCartaActual() {
        List<Plato> platos = platoRepository.findAll();
        List<Entrada> entradas = entradaRepository.findAll();

        ConfiguracionPrecio precio = configuracionPrecioRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    ConfiguracionPrecio nuevo = ConfiguracionPrecio.builder()
                            .menuCompleto(new BigDecimal("12.00"))
                            .soloSegundo(new BigDecimal("10.00"))
                            .build();
                    return configuracionPrecioRepository.save(nuevo);
                });

        return CartaDiariaDTO.builder()
                .platos(platos)
                .entradas(entradas)
                .precios(precio)
                .build();
    }

    @Transactional
    public Plato guardarPlato(Plato plato) {
        validarTurnoAbierto();
        if (plato.getActivo() == null) {
            plato.setActivo(true);
        }
        notificarCambiosMetricas();
        return platoRepository.save(plato);
    }

    @Transactional
    public void eliminarPlato(Long id) {
        validarTurnoAbierto();
        boolean tieneVentas = detalleVentaRepository.existsByPlatoId(id);

        if (tieneVentas) {
            Plato plato = platoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Plato no encontrado"));
            plato.setActivo(false);
            plato.setStock(0);
            platoRepository.save(plato);
        } else {
            platoRepository.deleteById(id);
        }
        notificarCambiosMetricas();
    }

    @Transactional
    public Entrada guardarEntrada(Entrada entrada) {
        validarTurnoAbierto();
        if (entrada.getActivo() == null) {
            entrada.setActivo(true);
        }
        notificarCambiosMetricas();
        return entradaRepository.save(entrada);
    }

    @Transactional
    public void eliminarEntrada(Long id) {
        validarTurnoAbierto();
        boolean tieneVentas = detalleVentaRepository.existsByEntradaId(id);

        if (tieneVentas) {
            Entrada entrada = entradaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Entrada no encontrada"));
            entrada.setActivo(false);
            entradaRepository.save(entrada);
        } else {
            entradaRepository.deleteById(id);
        }
        notificarCambiosMetricas();
    }

    @Transactional
    public ConfiguracionPrecio actualizarPrecios(ConfiguracionPrecio nuevosPrecios) {
        validarTurnoAbierto();
        notificarCambiosMetricas();
        return configuracionPrecioRepository.save(nuevosPrecios);
    }

    private void validarTurnoAbierto() {
        boolean hayTurno = turnoRepository.findTopByEstadoOrderByIdDesc("ABIERTO").isPresent();
        if (!hayTurno) {
            throw new RuntimeException("Acción bloqueada: No se puede modificar la carta sin un turno abierto.");
        }
    }

    @EventListener
    public void handleVentaRegistrada(VentaRegistradaEvent event) {
        try {
            messagingTemplate.convertAndSend("/topic/carta", obtenerCartaActual());
        } catch (Exception e) {
            System.err.println("Error al actualizar carta por WebSocket: " + e.getMessage());
        }
    }

    private void notificarCambiosMetricas() {
        try {
            messagingTemplate.convertAndSend("/topic/metricas", ventaService.obtenerMetricas());
        } catch (Exception e) {
            System.err.println("Error al emitir métricas por WebSocket: " + e.getMessage());
        }
    }
}