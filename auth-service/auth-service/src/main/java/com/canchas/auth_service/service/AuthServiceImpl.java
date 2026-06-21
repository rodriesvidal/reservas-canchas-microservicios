package com.canchas.auth_service.service;

import com.canchas.auth_service.dto.AuthResponse;
import com.canchas.auth_service.dto.LoginRequest;
import com.canchas.auth_service.dto.RegistroRequest;
import com.canchas.auth_service.exception.CredencialesInvalidasException;
import com.canchas.auth_service.exception.EmailYaRegistradoException;
import com.canchas.auth_service.model.Usuario;
import com.canchas.auth_service.repository.UsuarioRepository;
import com.canchas.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailYaRegistradoException(
                    "El email " + request.getEmail() + " ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());

        Usuario guardado = usuarioRepository.save(usuario);

        String token = jwtService.generarToken(guardado.getEmail(), guardado.getRol().name());
        return new AuthResponse(token, "Bearer", guardado.getEmail(), guardado.getRol().name());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CredencialesInvalidasException(
                        "Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("Email o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol().name());
        return new AuthResponse(token, "Bearer", usuario.getEmail(), usuario.getRol().name());
    }
}