package com.canchas.reserva_service.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoDTO {
    private Long id;
    private Long idReserva;
    private BigDecimal monto;
    private String estado;
}