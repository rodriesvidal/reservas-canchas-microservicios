package com.canchas.horario_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioResponse {

    private Long id;
    private Long idCancha;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Boolean disponible;
}