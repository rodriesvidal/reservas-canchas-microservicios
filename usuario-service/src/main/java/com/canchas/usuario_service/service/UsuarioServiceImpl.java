package com.canchas.usuario_service.service;

import com.canchas.usuario_service.dto.UsuarioRequest;
import com.canchas.usuario_service.dto.UsuarioResponse;
import com.canchas.usuario_service.exception.RecursoNoEncontradoException;
import com.canchas.usuario_service.model.Usuario;
import com.canchas.usuario_service.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;

    @Override
    public UsuarioResponse crear(UsuarioRequest req) {
        if (repository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        Usuario usuario = Usuario.builder()
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .email(req.getEmail())
                .telefono(req.getTelefono())
                .rol(req.getRol())
                .build();
        return toResponse(repository.save(usuario));
    }

    @Override
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        return toResponse(usuario);
    }

    @Override
    public UsuarioResponse actualizar(Long id, UsuarioRequest req) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        usuario.setNombre(req.getNombre());
        usuario.setApellido(req.getApellido());
        usuario.setEmail(req.getEmail());
        usuario.setTelefono(req.getTelefono());
        usuario.setRol(req.getRol());
        return toResponse(repository.save(usuario));
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado: " + id);
        }
        repository.deleteById(id);
    }

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .telefono(u.getTelefono())
                .rol(u.getRol())
                .fechaRegistro(u.getFechaRegistro())
                .build();
    }
}