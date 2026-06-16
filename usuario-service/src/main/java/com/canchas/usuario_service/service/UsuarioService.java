package com.canchas.usuario_service.service;

import com.canchas.usuario_service.dto.UsuarioRequest;
import com.canchas.usuario_service.dto.UsuarioResponse;
import java.util.List;

public interface UsuarioService {
    UsuarioResponse crear(UsuarioRequest req);
    List<UsuarioResponse> listar();
    UsuarioResponse obtenerPorId(Long id);
    UsuarioResponse actualizar(Long id, UsuarioRequest req);
    void eliminar(Long id);
}