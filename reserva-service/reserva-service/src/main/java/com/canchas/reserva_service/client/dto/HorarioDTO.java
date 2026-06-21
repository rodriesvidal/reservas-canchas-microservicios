package com.canchas.reserva_service.client.dto;

import lombok.Data;

@Data
public class HorarioDTO {
    private Long id;
    private Long idCancha;
    private Boolean disponible;
}