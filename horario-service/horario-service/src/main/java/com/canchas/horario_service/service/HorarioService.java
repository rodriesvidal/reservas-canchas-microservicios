package com.canchas.horario_service.service;

import com.canchas.horario_service.dto.HorarioRequest;
import com.canchas.horario_service.dto.HorarioResponse;

import java.util.List;

public interface HorarioService {

    HorarioResponse crear(HorarioRequest request);

    List<HorarioResponse> listarTodos();

    HorarioResponse obtenerPorId(Long id);

    List<HorarioResponse> listarPorCancha(Long idCancha);

    List<HorarioResponse> listarDisponiblesPorCancha(Long idCancha);

    HorarioResponse actualizar(Long id, HorarioRequest request);

    HorarioResponse cambiarDisponibilidad(Long id, boolean disponible);

    void eliminar(Long id);
}