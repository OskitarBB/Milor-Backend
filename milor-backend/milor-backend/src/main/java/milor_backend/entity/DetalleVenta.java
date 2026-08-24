package milor_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private VentaRegistro venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plato_id", nullable = false)
    private Plato plato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_id")
    private Entrada entrada;

    private String tipo; // COMPLETO o SOLO_SEGUNDO

    @Enumerated(EnumType.STRING)
    private ModalidadConsumo modalidad; // <--- Modalidad independiente por ítem

    private BigDecimal subtotal;
}