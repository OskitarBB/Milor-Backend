package milor_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "platos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "es_ilimitado", nullable = false)
    private Boolean esIlimitado;

    @Column(nullable = false)
    private Boolean activo;
}