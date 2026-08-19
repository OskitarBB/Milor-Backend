package milor_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_venta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @JsonIgnore
    private VentaRegistro venta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plato_id", nullable = false)
    private Plato plato;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entrada_id")
    private Entrada entrada; // Puede ser null si es "Sin Entrada"

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_menu", nullable = false, length = 20)
    private TipoMenu tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}