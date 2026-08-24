package milor_backend.dto;

import lombok.Data;
import milor_backend.entity.ModalidadConsumo;
import java.math.BigDecimal;

@Data
public class ItemVentaRequest {
    private Long platoId;
    private Long entradaId;
    private String tipo;
    private ModalidadConsumo modalidad; // <--- Recibe la modalidad independiente de este ítem
    private BigDecimal subtotal;
}