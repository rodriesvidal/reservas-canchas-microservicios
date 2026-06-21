package com.canchas.horario_service.service;

import com.canchas.horario_service.dto.HorarioRequest;
import com.canchas.horario_service.dto.HorarioResponse;
import com.canchas.horario_service.exception.RecursoNoEncontradoException;
import com.canchas.horario_service.model.Horario;
import com.canchas.horario_service.repository.HorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HorarioServiceImpl implements HorarioService {

    private final HorarioRepository horarioRepository;

    @Override
    public HorarioResponse crear(HorarioRequest request) {
        validarHoras(request);

        Horario horario = new Horario();
        horario.setIdCancha(request.getIdCancha());
        horario.setFecha(request.getFecha());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setDisponible(true);

        Horario guardado = horarioRepository.save(horario);
        return mapearAResponse(guardado);
    }

    @Override
    public List<HorarioResponse> listarTodos() {
        return horarioRepository.findAll()
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    public HorarioResponse obtenerPorId(Long id) {
        Horario horario = buscarHorario(id);
        return mapearAResponse(horario);
    }

    @Override
    public List<HorarioResponse> listarPorCancha(Long idCancha) {
        return horarioRepository.findByIdCancha(idCancha)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    public List<HorarioResponse> listarDisponiblesPorCancha(Long idCancha) {
        return horarioRepository.findByIdCanchaAndDisponibleTrue(idCancha)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    public HorarioResponse actualizar(Long id, HorarioRequest request) {
        validarHoras(request);

        Horario horario = buscarHorario(id);
        horario.setIdCancha(request.getIdCancha());
        horario.setFecha(request.getFecha());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        // disponible se mantiene como estaba

        Horario actualizado = horarioRepository.save(horario);
        return mapearAResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Horario horario = buscarHorario(id);
        horarioRepository.delete(horario);
    }

    // ---- Métodos privados de apoyo ----

    private Horario buscarHorario(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el horario con id " + id));
    }

    private void validarHoras(HorarioRequest request) {
        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new IllegalArgumentException(
                    "La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    @Override
    public HorarioResponse cambiarDisponibilidad(Long id, boolean disponible) {
        Horario horario = buscarHorario(id);
        horario.setDisponible(disponible);
        return mapearAResponse(horarioRepository.save(horario));
    }

    private HorarioResponse mapearAResponse(Horario horario) {
        return new HorarioResponse(
                horario.getId(),
                horario.getIdCancha(),
                horario.getFecha(),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.getDisponible()
        );
    }
}