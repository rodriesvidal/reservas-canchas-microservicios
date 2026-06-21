package com.canchas.auth_service.service;

import com.canchas.auth_service.dto.AuthResponse;
import com.canchas.auth_service.dto.LoginRequest;
import com.canchas.auth_service.dto.RegistroRequest;
import com.canchas.auth_service.exception.CredencialesInvalidasException;
import com.canchas.auth_service.exception.EmailYaRegistradoException;
import com.canchas.auth_service.model.Rol;
import com.canchas.auth_service.model.Usuario;
import com.canchas.auth_service.repository.UsuarioRepository;
import com.canchas.auth_service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios de AuthServiceImpl")
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl service;

    private RegistroRequest registroRequest;
    private LoginRequest loginRequest;
    private Usuario usuarioGuardado;

    @BeforeEach
    void setUp() {
        registroRequest = new RegistroRequest();
        registroRequest.setNombre("María García");
        registroRequest.setEmail("maria@correo.com");
        registroRequest.setPassword("pass123");
        registroRequest.setRol(Rol.CLIENTE);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("maria@correo.com");
        loginRequest.setPassword("pass123");

        usuarioGuardado = new Usuario(1L, "María García",
                "maria@correo.com", "hashedPassword", Rol.CLIENTE);
    }

    // ---- Tests para registrar() ----

    @Test
    @DisplayName("Registrar usuario con email nuevo debe retornar AuthResponse con token JWT")
    void registrar_emailNuevo_retornaAuthResponseConToken() {
        // Given
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(jwtService.generarToken(anyString(), anyString())).thenReturn("token.jwt.firmado");

        // When
        AuthResponse resultado = service.registrar(registroRequest);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getToken()).isEqualTo("token.jwt.firmado");
        assertThat(resultado.getEmail()).isEqualTo("maria@correo.com");
        assertThat(resultado.getRol()).isEqualTo("CLIENTE");
        verify(passwordEncoder).encode("pass123");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(jwtService).generarToken("maria@correo.com", "CLIENTE");
    }

    @Test
    @DisplayName("Registrar usuario con email ya registrado debe lanzar EmailYaRegistradoException")
    void registrar_emailDuplicado_lanzaExcepcion() {
        // Given
        when(usuarioRepository.existsByEmail("maria@correo.com")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.registrar(registroRequest))
                .isInstanceOf(EmailYaRegistradoException.class)
                .hasMessageContaining("maria@correo.com");
        verify(usuarioRepository, never()).save(any());
        verify(jwtService, never()).generarToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Registrar usuario debe encriptar la contraseña antes de guardar")
    void registrar_debeEncriptarPassword() {
        // Given
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$10$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(jwtService.generarToken(anyString(), anyString())).thenReturn("token");

        // When
        service.registrar(registroRequest);

        // Then
        verify(passwordEncoder).encode("pass123");
    }

    // ---- Tests para login() ----

    @Test
    @DisplayName("Login con credenciales válidas debe retornar AuthResponse con token JWT")
    void login_credencialesValidas_retornaToken() {
        // Given
        when(usuarioRepository.findByEmail("maria@correo.com")).thenReturn(Optional.of(usuarioGuardado));
        when(passwordEncoder.matches("pass123", "hashedPassword")).thenReturn(true);
        when(jwtService.generarToken(anyString(), anyString())).thenReturn("token.jwt.firmado");

        // When
        AuthResponse resultado = service.login(loginRequest);

        // Then
        assertThat(resultado.getToken()).isEqualTo("token.jwt.firmado");
        assertThat(resultado.getTipo()).isEqualTo("Bearer");
        assertThat(resultado.getEmail()).isEqualTo("maria@correo.com");
    }

    @Test
    @DisplayName("Login con email inexistente debe lanzar CredencialesInvalidasException")
    void login_emailInexistente_lanzaExcepcion() {
        // Given
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.login(loginRequest))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessageContaining("incorrectos");
        verify(jwtService, never()).generarToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta debe lanzar CredencialesInvalidasException")
    void login_passwordIncorrecta_lanzaExcepcion() {
        // Given
        when(usuarioRepository.findByEmail("maria@correo.com")).thenReturn(Optional.of(usuarioGuardado));
        when(passwordEncoder.matches("pass123", "hashedPassword")).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> service.login(loginRequest))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessageContaining("incorrectos");
        verify(jwtService, never()).generarToken(anyString(), anyString());
    }
}
