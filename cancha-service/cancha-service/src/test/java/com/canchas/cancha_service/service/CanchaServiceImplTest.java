package com.canchas.cancha_service.service;

import com.canchas.cancha_service.dto.CanchaRequest;
import com.canchas.cancha_service.dto.CanchaResponse;
import com.canchas.cancha_service.exception.RecursoNoEncontradoException;
import com.canchas.cancha_service.model.Cancha;
import com.canchas.cancha_service.model.EstadoCancha;
import com.canchas.cancha_service.model.TipoCancha;
import com.canchas.cancha_service.repository.CanchaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios de CanchaServiceImpl")
class CanchaServiceImplTest {

    @Mock
    private CanchaRepository repository;

    @InjectMocks
    private CanchaServiceImpl service;

    private Cancha canchaEjemplo;
    private CanchaRequest requestEjemplo;

    @BeforeEach
    void setUp() {
        canchaEjemplo = Cancha.builder()
                .id(1L)
                .nombre("Cancha Norte")
                .tipo(TipoCancha.FUTBOL)
                .ubicacion("Av. Principal 123")
                .precioHora(new BigDecimal("15000"))
                .idPropietario(10L)
                .estado(EstadoCancha.DISPONIBLE)
                .build();

        requestEjemplo = new CanchaRequest();
        requestEjemplo.setNombre("Cancha Norte");
        requestEjemplo.setTipo(TipoCancha.FUTBOL);
        requestEjemplo.setUbicacion("Av. Principal 123");
        requestEjemplo.setPrecioHora(new BigDecimal("15000"));
        requestEjemplo.setIdPropietario(10L);
        requestEjemplo.setEstado(EstadoCancha.DISPONIBLE);
    }

    // ---- Tests para crear() ----

    @Test
    @DisplayName("Crear cancha con estado explícito debe retornar CanchaResponse con ese estado")
    void crear_conEstadoExplicito_retornaConEstadoCorrecto() {
        // Given
        when(repository.save(any(Cancha.class))).thenReturn(canchaEjemplo);

        // When
        CanchaResponse resultado = service.crear(requestEjemplo);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Cancha Norte");
        assertThat(resultado.getEstado()).isEqualTo(EstadoCancha.DISPONIBLE);
        assertThat(resultado.getPrecioHora()).isEqualByComparingTo(new BigDecimal("15000"));
        verify(repository).save(any(Cancha.class));
    }

    @Test
    @DisplayName("Crear cancha sin estado debe asignar DISPONIBLE por defecto")
    void crear_sinEstado_asignaDisponiblePorDefecto() {
        // Given
        requestEjemplo.setEstado(null);
        when(repository.save(any(Cancha.class))).thenReturn(canchaEjemplo);

        // When
        CanchaResponse resultado = service.crear(requestEjemplo);

        // Then
        assertThat(resultado.getEstado()).isEqualTo(EstadoCancha.DISPONIBLE);
    }

    // ---- Tests para listar() ----

    @Test
    @DisplayName("Listar canchas debe retornar todas las canchas existentes")
    void listar_retornaTodasLasCanchas() {
        // Given
        Cancha cancha2 = Cancha.builder()
                .id(2L).nombre("Cancha Sur").tipo(TipoCancha.PADEL)
                .ubicacion("Calle Sur 45").precioHora(new BigDecimal("12000"))
                .idPropietario(10L).estado(EstadoCancha.DISPONIBLE).build();
        when(repository.findAll()).thenReturn(List.of(canchaEjemplo, cancha2));

        // When
        List<CanchaResponse> resultado = service.listar();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Cancha Norte");
        assertThat(resultado.get(1).getNombre()).isEqualTo("Cancha Sur");
    }

    // ---- Tests para obtenerPorId() ----

    @Test
    @DisplayName("Obtener cancha por ID existente debe retornar CanchaResponse")
    void obtenerPorId_idExistente_retornaCancha() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(canchaEjemplo));

        // When
        CanchaResponse resultado = service.obtenerPorId(1L);

        // Then
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTipo()).isEqualTo(TipoCancha.FUTBOL);
    }

    @Test
    @DisplayName("Obtener cancha por ID inexistente debe lanzar RecursoNoEncontradoException")
    void obtenerPorId_idInexistente_lanzaExcepcion() {
        // Given
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    // ---- Tests para listarPorPropietario() ----

    @Test
    @DisplayName("Listar por propietario debe retornar solo canchas del propietario")
    void listarPorPropietario_retornaCancharDelPropietario() {
        // Given
        when(repository.findByIdPropietario(10L)).thenReturn(List.of(canchaEjemplo));

        // When
        List<CanchaResponse> resultado = service.listarPorPropietario(10L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdPropietario()).isEqualTo(10L);
    }

    // ---- Tests para actualizar() ----

    @Test
    @DisplayName("Actualizar cancha existente debe retornar datos actualizados")
    void actualizar_canchaExistente_retornaActualizada() {
        // Given
        CanchaRequest nuevosDatos = new CanchaRequest();
        nuevosDatos.setNombre("Cancha Renovada");
        nuevosDatos.setTipo(TipoCancha.FUTBOL);
        nuevosDatos.setUbicacion("Nueva Dirección 999");
        nuevosDatos.setPrecioHora(new BigDecimal("18000"));
        nuevosDatos.setIdPropietario(10L);
        nuevosDatos.setEstado(EstadoCancha.INACTIVA);

        Cancha canchaActualizada = Cancha.builder()
                .id(1L).nombre("Cancha Renovada").tipo(TipoCancha.FUTBOL)
                .ubicacion("Nueva Dirección 999").precioHora(new BigDecimal("18000"))
                .idPropietario(10L).estado(EstadoCancha.INACTIVA).build();

        when(repository.findById(1L)).thenReturn(Optional.of(canchaEjemplo));
        when(repository.save(any(Cancha.class))).thenReturn(canchaActualizada);

        // When
        CanchaResponse resultado = service.actualizar(1L, nuevosDatos);

        // Then
        assertThat(resultado.getNombre()).isEqualTo("Cancha Renovada");
        assertThat(resultado.getEstado()).isEqualTo(EstadoCancha.INACTIVA);
        assertThat(resultado.getPrecioHora()).isEqualByComparingTo(new BigDecimal("18000"));
    }

    @Test
    @DisplayName("Actualizar cancha inexistente debe lanzar RecursoNoEncontradoException")
    void actualizar_canchaInexistente_lanzaExcepcion() {
        // Given
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.actualizar(99L, requestEjemplo))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ---- Tests para eliminar() ----

    @Test
    @DisplayName("Eliminar cancha existente debe invocar deleteById")
    void eliminar_canchaExistente_invocaDeleteById() {
        // Given
        when(repository.existsById(1L)).thenReturn(true);

        // When
        service.eliminar(1L);

        // Then
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar cancha inexistente debe lanzar RecursoNoEncontradoException")
    void eliminar_canchaInexistente_lanzaExcepcion() {
        // Given
        when(repository.existsById(anyLong())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
        verify(repository, never()).deleteById(anyLong());
    }
}
