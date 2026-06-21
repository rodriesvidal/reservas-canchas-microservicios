package com.canchas.pago_service.service;

import com.canchas.pago_service.dto.PagoRequest;
import com.canchas.pago_service.dto.PagoResponse;

import java.util.List;

public interface PagoService {

    PagoResponse crear(PagoRequest request);

    List<PagoResponse> listarTodos();

    PagoResponse obtenerPorId(Long id);

    List<PagoResponse> listarPorReserva(Long idReserva);

    PagoResponse actualizar(Long id, PagoRequest request);

    PagoResponse confirmar(Long id);

    PagoResponse rechazar(Long id);

    void eliminar(Long id);
}