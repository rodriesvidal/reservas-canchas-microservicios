package com.canchas.usuario_service.dto;

import com.canchas.usuario_service.model.Rol;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    private String telefono;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
}