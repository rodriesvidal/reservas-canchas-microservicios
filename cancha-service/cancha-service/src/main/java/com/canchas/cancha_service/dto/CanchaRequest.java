package com.canchas.cancha_service.dto;

import com.canchas.cancha_service.model.EstadoCancha;
import com.canchas.cancha_service.model.TipoCancha;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CanchaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El tipo es obligatorio")
    private TipoCancha tipo;

    @NotBlank(message = "La ubicación es obligatoria")
    private String ubicacion;

    @NotNull(message = "El precio por hora es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precioHora;

    @NotNull(message = "El id del propietario es obligatorio")
    private Long idPropietario;

    private EstadoCancha estado;   // opcional: si no viene, queda DISPONIBLE
}