package milor_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import milor_backend.entity.TipoMenu;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemVentaRequest {

    @NotNull(message = "El ID del plato es obligatorio")
    private Long platoId;

    private Long entradaId; // Opcional (null = sin entrada)

    @NotNull(message = "El tipo de menú es obligatorio")
    private TipoMenu tipo;

    @NotNull(message = "El subtotal es obligatorio")
    private BigDecimal subtotal;
}