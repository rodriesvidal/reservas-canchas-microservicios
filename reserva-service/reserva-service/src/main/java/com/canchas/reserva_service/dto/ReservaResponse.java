package com.canchas.reserva_service.dto;

import com.canchas.reserva_service.model.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponse {

    private Long id;
    private Long idCliente;
    private Long idCancha;
    private Long idHorario;
    private BigDecimal monto;
    private EstadoReserva estado;
    private LocalDateTime fechaReserva;
    private Long idPago;
}