package com.canchas.horario_service.service;

import com.canchas.horario_service.dto.HorarioRequest;
import com.canchas.horario_service.dto.HorarioResponse;
import com.canchas.horario_service.exception.RecursoNoEncontradoException;
import com.canchas.horario_service.model.Horario;
import com.canchas.horario_service.repository.HorarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios de HorarioServiceImpl")
class HorarioServiceImplTest {

    @Mock
    private HorarioRepository horarioRepository;

    @InjectMocks
    private HorarioServiceImpl service;

    private Horario horarioEjemplo;
    private HorarioRequest requestValido;

    @BeforeEach
    void setUp() {
        horarioEjemplo = new Horario(
                1L, 5L,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                true
        );

        requestValido = new HorarioRequest();
        requestValido.setIdCancha(5L);
        requestValido.setFecha(LocalDate.of(2026, 7, 10));
        requestValido.setHoraInicio(LocalTime.of(10, 0));
        requestValido.setHoraFin(LocalTime.of(11, 0));
    }

    // ---- Tests para crear() ----

    @Test
    @DisplayName("Crear horario con horas válidas debe retornar HorarioResponse disponible")
    void crear_horasValidas_retornaHorarioDisponible() {
        // Given
        when(horarioRepository.save(any(Horario.class))).thenReturn(horarioEjemplo);

        // When
        HorarioResponse resultado = service.crear(requestValido);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getDisponible()).isTrue();
        assertThat(resultado.getIdCancha()).isEqualTo(5L);
        assertThat(resultado.getHoraInicio()).isEqualTo(LocalTime.of(10, 0));
        verify(horarioRepository).save(any(Horario.class));
    }

    @Test
    @DisplayName("Crear horario con horaFin igual a horaInicio debe lanzar IllegalArgumentException")
    void crear_horaFinIgualHoraInicio_lanzaExcepcion() {
        // Given
        requestValido.setHoraInicio(LocalTime.of(10, 0));
        requestValido.setHoraFin(LocalTime.of(10, 0));

        // When / Then
        assertThatThrownBy(() -> service.crear(requestValido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hora de fin");
        verify(horarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear horario con horaFin anterior a horaInicio debe lanzar IllegalArgumentException")
    void crear_horaFinAnteriorHoraInicio_lanzaExcepcion() {
        // Given
        requestValido.setHoraInicio(LocalTime.of(14, 0));
        requestValido.setHoraFin(LocalTime.of(10, 0));

        // When / Then
        assertThatThrownBy(() -> service.crear(requestValido))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- Tests para obtenerPorId() ----

    @Test
    @DisplayName("Obtener horario por ID existente debe retornar HorarioResponse")
    void obtenerPorId_idExistente_retornaHorario() {
        // Given
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioEjemplo));

        // When
        HorarioResponse resultado = service.obtenerPorId(1L);

        // Then
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getFecha()).isEqualTo(LocalDate.of(2026, 7, 10));
    }

    @Test
    @DisplayName("Obtener horario por ID inexistente debe lanzar RecursoNoEncontradoException")
    void obtenerPorId_idInexistente_lanzaExcepcion() {
        // Given
        when(horarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    // ---- Tests para listarDisponiblesPorCancha() ----

    @Test
    @DisplayName("Listar disponibles por cancha debe retornar solo horarios disponibles")
    void listarDisponiblesPorCancha_retornaSoloDisponibles() {
        // Given
        when(horarioRepository.findByIdCanchaAndDisponibleTrue(5L))
                .thenReturn(List.of(horarioEjemplo));

        // When
        List<HorarioResponse> resultado = service.listarDisponiblesPorCancha(5L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getDisponible()).isTrue();
    }

    // ---- Tests para cambiarDisponibilidad() ----

    @Test
    @DisplayName("Cambiar disponibilidad a false debe marcar horario como no disponible")
    void cambiarDisponibilidad_aFalse_marcaNoDisponible() {
        // Given
        Horario horarioActualizado = new Horario(1L, 5L,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(10, 0), LocalTime.of(11, 0), false);
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioEjemplo));
        when(horarioRepository.save(any(Horario.class))).thenReturn(horarioActualizado);

        // When
        HorarioResponse resultado = service.cambiarDisponibilidad(1L, false);

        // Then
        assertThat(resultado.getDisponible()).isFalse();
        verify(horarioRepository).save(any(Horario.class));
    }

    @Test
    @DisplayName("Cambiar disponibilidad de horario inexistente debe lanzar RecursoNoEncontradoException")
    void cambiarDisponibilidad_horarioInexistente_lanzaExcepcion() {
        // Given
        when(horarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.cambiarDisponibilidad(99L, true))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ---- Tests para eliminar() ----

    @Test
    @DisplayName("Eliminar horario existente debe invocar delete en el repositorio")
    void eliminar_horarioExistente_invocaDelete() {
        // Given
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioEjemplo));

        // When
        service.eliminar(1L);

        // Then
        verify(horarioRepository).delete(horarioEjemplo);
    }
}
