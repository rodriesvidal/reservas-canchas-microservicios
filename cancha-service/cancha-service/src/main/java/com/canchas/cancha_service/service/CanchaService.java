package com.canchas.cancha_service.service;

import com.canchas.cancha_service.dto.CanchaRequest;
import com.canchas.cancha_service.dto.CanchaResponse;
import java.util.List;

public interface CanchaService {
    CanchaResponse crear(CanchaRequest req);
    List<CanchaResponse> listar();
    CanchaResponse obtenerPorId(Long id);
    List<CanchaResponse> listarPorPropietario(Long idPropietario);
    CanchaResponse actualizar(Long id, CanchaRequest req);
    void eliminar(Long id);
}