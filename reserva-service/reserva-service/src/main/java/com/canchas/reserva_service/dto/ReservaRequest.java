package com.canchas.reserva_service.dto;

import com.canchas.reserva_service.model.MetodoPago;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservaRequest {

    @NotNull(message = "El id del cliente es obligatorio")
    private Long idCliente;

    @NotNull(message = "El id de la cancha es obligatorio")
    private Long idCancha;

    @NotNull(message = "El id del horario es obligatorio")
    private Long idHorario;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;
}