package com.canchas.pago_service.dto;

import com.canchas.pago_service.model.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoRequest {

    @NotNull(message = "El id de la reserva es obligatorio")
    private Long idReserva;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;
}