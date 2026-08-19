package milor_backend.dto;

import lombok.*;
import milor_backend.entity.ConfiguracionPrecio;
import milor_backend.entity.Entrada;
import milor_backend.entity.Plato;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartaDiariaDTO {
    private ConfiguracionPrecio precios;
    private List<Entrada> entradas;
    private List<Plato> platos;
}