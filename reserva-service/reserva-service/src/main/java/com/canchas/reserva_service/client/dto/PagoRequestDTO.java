package com.canchas.reserva_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequestDTO {
    private Long idReserva;
    private BigDecimal monto;
    private String metodoPago;
}