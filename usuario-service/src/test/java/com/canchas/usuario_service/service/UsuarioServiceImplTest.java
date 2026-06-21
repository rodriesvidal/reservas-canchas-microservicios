package com.canchas.usuario_service.service;

import com.canchas.usuario_service.dto.UsuarioRequest;
import com.canchas.usuario_service.dto.UsuarioResponse;
import com.canchas.usuario_service.exception.RecursoNoEncontradoException;
import com.canchas.usuario_service.model.Rol;
import com.canchas.usuario_service.model.Usuario;
import com.canchas.usuario_service.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios de UsuarioServiceImpl")
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioServiceImpl service;

    private Usuario usuarioEjemplo;
    private UsuarioRequest requestEjemplo;

    @BeforeEach
    void setUp() {
        usuarioEjemplo = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@correo.com")
                .telefono("912345678")
                .rol(Rol.CLIENTE)
                .fechaRegistro(LocalDateTime.now())
                .build();

        requestEjemplo = new UsuarioRequest();
        requestEjemplo.setNombre("Juan");
        requestEjemplo.setApellido("Pérez");
        requestEjemplo.setEmail("juan@correo.com");
        requestEjemplo.setTelefono("912345678");
        requestEjemplo.setRol(Rol.CLIENTE);
    }

    // ---- Tests para crear() ----

    @Test
    @DisplayName("Crear usuario con email nuevo debe retornar UsuarioResponse")
    void crear_emailNuevo_retornaResponse() {
        // Given
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenReturn(usuarioEjemplo);

        // When
        UsuarioResponse resultado = service.crear(requestEjemplo);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo("juan@correo.com");
        assertThat(resultado.getNombre()).isEqualTo("Juan");
        assertThat(resultado.getRol()).isEqualTo(Rol.CLIENTE);
        verify(repository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Crear usuario con email duplicado debe lanzar IllegalArgumentException")
    void crear_emailDuplicado_lanzaExcepcion() {
        // Given
        when(repository.existsByEmail(anyString())).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.crear(requestEjemplo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un usuario con ese email");
        verify(repository, never()).save(any());
    }

    // ---- Tests para listar() ----

    @Test
    @DisplayName("Listar usuarios debe retornar lista con todos los usuarios")
    void listar_retornaListaUsuarios() {
        // Given
        Usuario usuario2 = Usuario.builder()
                .id(2L).nombre("Ana").apellido("López")
                .email("ana@correo.com").rol(Rol.PROPIETARIO)
                .fechaRegistro(LocalDateTime.now()).build();
        when(repository.findAll()).thenReturn(List.of(usuarioEjemplo, usuario2));

        // When
        List<UsuarioResponse> resultado = service.listar();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getEmail()).isEqualTo("juan@correo.com");
        assertThat(resultado.get(1).getEmail()).isEqualTo("ana@correo.com");
    }

    @Test
    @DisplayName("Listar usuarios cuando no hay registros debe retornar lista vacía")
    void listar_sinUsuarios_retornaListaVacia() {
        // Given
        when(repository.findAll()).thenReturn(List.of());

        // When
        List<UsuarioResponse> resultado = service.listar();

        // Then
        assertThat(resultado).isEmpty();
    }

    // ---- Tests para obtenerPorId() ----

    @Test
    @DisplayName("Obtener usuario por ID existente debe retornar UsuarioResponse")
    void obtenerPorId_idExistente_retornaResponse() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioEjemplo));

        // When
        UsuarioResponse resultado = service.obtenerPorId(1L);

        // Then
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Juan");
    }

    @Test
    @DisplayName("Obtener usuario por ID inexistente debe lanzar RecursoNoEncontradoException")
    void obtenerPorId_idInexistente_lanzaExcepcion() {
        // Given
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    // ---- Tests para actualizar() ----

    @Test
    @DisplayName("Actualizar usuario existente debe retornar datos actualizados")
    void actualizar_usuarioExistente_retornaActualizado() {
        // Given
        UsuarioRequest nuevosDatos = new UsuarioRequest();
        nuevosDatos.setNombre("Juan Actualizado");
        nuevosDatos.setApellido("Pérez");
        nuevosDatos.setEmail("juan@correo.com");
        nuevosDatos.setTelefono("999999999");
        nuevosDatos.setRol(Rol.CLIENTE);

        Usuario usuarioActualizado = Usuario.builder()
                .id(1L).nombre("Juan Actualizado").apellido("Pérez")
                .email("juan@correo.com").telefono("999999999")
                .rol(Rol.CLIENTE).fechaRegistro(LocalDateTime.now()).build();

        when(repository.findById(1L)).thenReturn(Optional.of(usuarioEjemplo));
        when(repository.save(any(Usuario.class))).thenReturn(usuarioActualizado);

        // When
        UsuarioResponse resultado = service.actualizar(1L, nuevosDatos);

        // Then
        assertThat(resultado.getNombre()).isEqualTo("Juan Actualizado");
        assertThat(resultado.getTelefono()).isEqualTo("999999999");
        verify(repository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Actualizar usuario inexistente debe lanzar RecursoNoEncontradoException")
    void actualizar_usuarioInexistente_lanzaExcepcion() {
        // Given
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.actualizar(99L, requestEjemplo))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ---- Tests para eliminar() ----

    @Test
    @DisplayName("Eliminar usuario existente debe invocar deleteById en el repositorio")
    void eliminar_usuarioExistente_invocaDeleteById() {
        // Given
        when(repository.existsById(1L)).thenReturn(true);

        // When
        service.eliminar(1L);

        // Then
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar usuario inexistente debe lanzar RecursoNoEncontradoException")
    void eliminar_usuarioInexistente_lanzaExcepcion() {
        // Given
        when(repository.existsById(anyLong())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
        verify(repository, never()).deleteById(anyLong());
    }
}
