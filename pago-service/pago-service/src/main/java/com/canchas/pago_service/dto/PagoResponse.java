package com.canchas.pago_service.dto;

import com.canchas.pago_service.model.EstadoPago;
import com.canchas.pago_service.model.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {

    private Long id;
    private Long idReserva;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private EstadoPago estado;
    private LocalDateTime fechaPago;
}