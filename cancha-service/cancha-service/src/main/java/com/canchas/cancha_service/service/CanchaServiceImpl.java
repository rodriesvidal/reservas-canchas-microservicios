package com.canchas.cancha_service.service;

import com.canchas.cancha_service.dto.CanchaRequest;
import com.canchas.cancha_service.dto.CanchaResponse;
import com.canchas.cancha_service.exception.RecursoNoEncontradoException;
import com.canchas.cancha_service.model.Cancha;
import com.canchas.cancha_service.model.EstadoCancha;
import com.canchas.cancha_service.repository.CanchaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CanchaServiceImpl implements CanchaService {

    private final CanchaRepository repository;

    @Override
    public CanchaResponse crear(CanchaRequest req) {
        Cancha cancha = Cancha.builder()
                .nombre(req.getNombre())
                .tipo(req.getTipo())
                .ubicacion(req.getUbicacion())
                .precioHora(req.getPrecioHora())
                .idPropietario(req.getIdPropietario())
                .estado(req.getEstado() != null ? req.getEstado() : EstadoCancha.DISPONIBLE)
                .build();
        return toResponse(repository.save(cancha));
    }

    @Override
    public List<CanchaResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public CanchaResponse obtenerPorId(Long id) {
        Cancha cancha = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cancha no encontrada: " + id));
        return toResponse(cancha);
    }

    @Override
    public List<CanchaResponse> listarPorPropietario(Long idPropietario) {
        return repository.findByIdPropietario(idPropietario).stream().map(this::toResponse).toList();
    }

    @Override
    public CanchaResponse actualizar(Long id, CanchaRequest req) {
        Cancha cancha = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cancha no encontrada: " + id));
        cancha.setNombre(req.getNombre());
        cancha.setTipo(req.getTipo());
        cancha.setUbicacion(req.getUbicacion());
        cancha.setPrecioHora(req.getPrecioHora());
        cancha.setIdPropietario(req.getIdPropietario());
        if (req.getEstado() != null) {
            cancha.setEstado(req.getEstado());
        }
        return toResponse(repository.save(cancha));
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Cancha no encontrada: " + id);
        }
        repository.deleteById(id);
    }

    private CanchaResponse toResponse(Cancha c) {
        return CanchaResponse.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .tipo(c.getTipo())
                .ubicacion(c.getUbicacion())
                .precioHora(c.getPrecioHora())
                .idPropietario(c.getIdPropietario())
                .estado(c.getEstado())
                .build();
    }
}