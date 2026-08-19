package milor_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "configuracion_precios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_completo", nullable = false, precision = 10, scale = 2)
    private BigDecimal menuCompleto;

    @Column(name = "solo_segundo", nullable = false, precision = 10, scale = 2)
    private BigDecimal soloSegundo;
}