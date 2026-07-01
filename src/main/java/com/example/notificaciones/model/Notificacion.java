package com.example.notificaciones.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mensaje;
    private String perfilReceptor; // Ej: "Operador AGE" o "Registrador"
    private LocalDate fechaPlazo;
    private boolean leida;
}
