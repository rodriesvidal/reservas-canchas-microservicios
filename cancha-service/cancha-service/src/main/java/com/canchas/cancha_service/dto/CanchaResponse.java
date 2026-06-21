package com.canchas.cancha_service.dto;

import com.canchas.cancha_service.model.EstadoCancha;
import com.canchas.cancha_service.model.TipoCancha;
import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CanchaResponse {
    private Long id;
    private String nombre;
    private TipoCancha tipo;
    private String ubicacion;
    private BigDecimal precioHora;
    private Long idPropietario;
    private EstadoCancha estado;
}