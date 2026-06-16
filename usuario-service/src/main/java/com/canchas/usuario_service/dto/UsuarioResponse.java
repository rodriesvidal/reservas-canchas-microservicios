package com.canchas.usuario_service.dto;

import com.canchas.usuario_service.model.Rol;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private Rol rol;
    private LocalDateTime fechaRegistro;
}