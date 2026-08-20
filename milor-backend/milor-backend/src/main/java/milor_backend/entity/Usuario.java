package milor_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // En producción usar encriptación (BCrypt), aquí plano o simple para el POS local

    @Column(nullable = false)
    private String rol; // 'MESERO', 'ADMIN', 'SOPORTE'
}