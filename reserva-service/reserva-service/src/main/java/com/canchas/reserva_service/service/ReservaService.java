package com.canchas.reserva_service.service;

import com.canchas.reserva_service.dto.ReservaRequest;
import com.canchas.reserva_service.dto.ReservaResponse;

import java.util.List;

public interface ReservaService {

    ReservaResponse crear(ReservaRequest request);

    List<ReservaResponse> listarTodos();

    ReservaResponse obtenerPorId(Long id);

    List<ReservaResponse> listarPorCliente(Long idCliente);

    ReservaResponse cancelar(Long id);
}