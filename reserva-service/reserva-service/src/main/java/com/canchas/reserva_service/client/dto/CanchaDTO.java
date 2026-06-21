package com.canchas.reserva_service.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CanchaDTO {
    private Long id;
    private String nombre;
    private BigDecimal precioHora;
    private String estado;
}