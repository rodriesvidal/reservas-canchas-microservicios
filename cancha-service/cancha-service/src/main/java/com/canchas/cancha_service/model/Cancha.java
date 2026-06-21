package com.canchas.cancha_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "canchas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cancha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCancha tipo;

    @Column(nullable = false)
    private String ubicacion;

    @Column(name = "precio_hora", nullable = false)
    private BigDecimal precioHora;

    @Column(name = "id_propietario", nullable = false)
    private Long idPropietario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCancha estado;
}