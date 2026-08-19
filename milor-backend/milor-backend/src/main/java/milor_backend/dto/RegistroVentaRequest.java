package milor_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import milor_backend.entity.ModalidadConsumo;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroVentaRequest {

    @NotNull(message = "La modalidad es obligatoria")
    private ModalidadConsumo modalidad;

    @NotEmpty(message = "Debe incluir al menos un plato en la venta")
    @Valid
    private List<ItemVentaRequest> items;
}