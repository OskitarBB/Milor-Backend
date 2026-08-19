package milor_backend.service;

import lombok.RequiredArgsConstructor;
import milor_backend.dto.CartaDiariaDTO;
import milor_backend.entity.ConfiguracionPrecio;
import milor_backend.entity.Entrada;
import milor_backend.entity.Plato;
import milor_backend.repository.ConfiguracionPrecioRepository;
import milor_backend.repository.EntradaRepository;
import milor_backend.repository.PlatoRepository;
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

    @Transactional
    public CartaDiariaDTO obtenerCartaActual() {
        List<Plato> platos = platoRepository.findByActivoTrue();
        List<Entrada> entradas = entradaRepository.findByActivoTrue();

        ConfiguracionPrecio precio = configuracionPrecioRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> configuracionPrecioRepository.save(
                        ConfiguracionPrecio.builder()
                                .menuCompleto(new BigDecimal("12.00"))
                                .soloSegundo(new BigDecimal("10.00"))
                                .build()
                ));

        return CartaDiariaDTO.builder()
                .platos(platos)
                .entradas(entradas)
                .precios(precio)
                .build();
    }

    @Transactional
    public Plato guardarPlato(Plato plato) {
        return platoRepository.save(plato);
    }

    @Transactional
    public void eliminarPlato(Long id) {
        platoRepository.deleteById(id);
    }

    @Transactional
    public Entrada guardarEntrada(Entrada entrada) {
        return entradaRepository.save(entrada);
    }

    @Transactional
    public void eliminarEntrada(Long id) {
        entradaRepository.deleteById(id);
    }

    @Transactional
    public ConfiguracionPrecio actualizarPrecios(ConfiguracionPrecio nuevosPrecios) {
        return configuracionPrecioRepository.save(nuevosPrecios);
    }
}