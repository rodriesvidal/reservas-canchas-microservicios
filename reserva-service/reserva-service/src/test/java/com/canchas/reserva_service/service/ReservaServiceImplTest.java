package com.canchas.reserva_service.service;

import com.canchas.reserva_service.client.CanchaClient;
import com.canchas.reserva_service.client.HorarioClient;
import com.canchas.reserva_service.client.PagoClient;
import com.canchas.reserva_service.client.UsuarioClient;
import com.canchas.reserva_service.client.dto.CanchaDTO;
import com.canchas.reserva_service.client.dto.HorarioDTO;
import com.canchas.reserva_service.client.dto.PagoDTO;
import com.canchas.reserva_service.client.dto.UsuarioDTO;
import com.canchas.reserva_service.dto.ReservaRequest;
import com.canchas.reserva_service.dto.ReservaResponse;
import com.canchas.reserva_service.exception.HorarioNoDisponibleException;
import com.canchas.reserva_service.exception.RecursoNoEncontradoException;
import com.canchas.reserva_service.model.EstadoReserva;
import com.canchas.reserva_service.model.MetodoPago;
import com.canchas.reserva_service.model.Reserva;
import com.canchas.reserva_service.repository.ReservaRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios de ReservaServiceImpl")
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private CanchaClient canchaClient;

    @Mock
    private HorarioClient horarioClient;

    @Mock
    private PagoClient pagoClient;

    @InjectMocks
    private ReservaServiceImpl service;

    private ReservaRequest requestEjemplo;
    private Reserva reservaGuardada;
    private CanchaDTO canchaDTO;
    private HorarioDTO horarioDTO;
    private PagoDTO pagoDTO;

    @BeforeEach
    void setUp() {
        requestEjemplo = new ReservaRequest();
        requestEjemplo.setIdCliente(1L);
        requestEjemplo.setIdCancha(5L);
        requestEjemplo.setIdHorario(10L);
        requestEjemplo.setMetodoPago(MetodoPago.TARJETA);

        canchaDTO = new CanchaDTO();
        canchaDTO.setId(5L);
        canchaDTO.setNombre("Cancha Norte");
        canchaDTO.setPrecioHora(new BigDecimal("15000"));
        canchaDTO.setEstado("DISPONIBLE");

        horarioDTO = new HorarioDTO();
        horarioDTO.setId(10L);
        horarioDTO.setIdCancha(5L);
        horarioDTO.setDisponible(true);

        pagoDTO = new PagoDTO();
        pagoDTO.setId(200L);
        pagoDTO.setIdReserva(50L);
        pagoDTO.setMonto(new BigDecimal("15000"));
        pagoDTO.setEstado("PENDIENTE");

        reservaGuardada = new Reserva(
                50L, 1L, 5L, 10L,
                new BigDecimal("15000"),
                EstadoReserva.CONFIRMADA,
                LocalDateTime.now(), null
        );
    }

    // ---- Tests para crear() ----

    @Test
    @DisplayName("Crear reserva válida debe orquestar todos los microservicios y retornar ReservaResponse")
    void crear_reservaValida_orchestraTodosLosServicios() {
        // Given
        Reserva reservaConPago = new Reserva(50L, 1L, 5L, 10L,
                new BigDecimal("15000"), EstadoReserva.CONFIRMADA,
                LocalDateTime.now(), 200L);

        when(usuarioClient.obtenerUsuario(1L)).thenReturn(new UsuarioDTO());
        when(canchaClient.obtenerCancha(5L)).thenReturn(canchaDTO);
        when(horarioClient.obtenerHorario(10L)).thenReturn(horarioDTO);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaGuardada).thenReturn(reservaConPago);
        when(pagoClient.crearPago(any())).thenReturn(pagoDTO);

        // When
        ReservaResponse resultado = service.crear(requestEjemplo);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(resultado.getMonto()).isEqualByComparingTo(new BigDecimal("15000"));
        verify(usuarioClient).obtenerUsuario(1L);
        verify(canchaClient).obtenerCancha(5L);
        verify(horarioClient).obtenerHorario(10L);
        verify(pagoClient).crearPago(any());
        verify(horarioClient).cambiarDisponibilidad(10L, false);
        verify(reservaRepository, times(2)).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Crear reserva con cliente inexistente debe lanzar RecursoNoEncontradoException")
    void crear_clienteInexistente_lanzaExcepcion() {
        // Given
        when(usuarioClient.obtenerUsuario(anyLong()))
                .thenThrow(mock(FeignException.NotFound.class));

        // When / Then
        assertThatThrownBy(() -> service.crear(requestEjemplo))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("cliente");
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear reserva con cancha inexistente debe lanzar RecursoNoEncontradoException")
    void crear_canchaInexistente_lanzaExcepcion() {
        // Given
        when(usuarioClient.obtenerUsuario(anyLong())).thenReturn(new UsuarioDTO());
        when(canchaClient.obtenerCancha(anyLong()))
                .thenThrow(mock(FeignException.NotFound.class));

        // When / Then
        assertThatThrownBy(() -> service.crear(requestEjemplo))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("cancha");
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear reserva con horario no disponible debe lanzar HorarioNoDisponibleException")
    void crear_horarioNoDisponible_lanzaExcepcion() {
        // Given
        horarioDTO.setDisponible(false);
        when(usuarioClient.obtenerUsuario(anyLong())).thenReturn(new UsuarioDTO());
        when(canchaClient.obtenerCancha(anyLong())).thenReturn(canchaDTO);
        when(horarioClient.obtenerHorario(anyLong())).thenReturn(horarioDTO);

        // When / Then
        assertThatThrownBy(() -> service.crear(requestEjemplo))
                .isInstanceOf(HorarioNoDisponibleException.class)
                .hasMessageContaining("ya está reservado");
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear reserva con horario inexistente debe lanzar RecursoNoEncontradoException")
    void crear_horarioInexistente_lanzaExcepcion() {
        // Given
        when(usuarioClient.obtenerUsuario(anyLong())).thenReturn(new UsuarioDTO());
        when(canchaClient.obtenerCancha(anyLong())).thenReturn(canchaDTO);
        when(horarioClient.obtenerHorario(anyLong()))
                .thenThrow(mock(FeignException.NotFound.class));

        // When / Then
        assertThatThrownBy(() -> service.crear(requestEjemplo))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("horario");
    }

    // ---- Tests para cancelar() ----

    @Test
    @DisplayName("Cancelar reserva debe cambiar estado a CANCELADA y liberar el horario")
    void cancelar_reservaExistente_cambiaEstadoYLiberaHorario() {
        // Given
        Reserva reservaCancelada = new Reserva(50L, 1L, 5L, 10L,
                new BigDecimal("15000"), EstadoReserva.CANCELADA,
                LocalDateTime.now(), 200L);
        when(reservaRepository.findById(50L)).thenReturn(Optional.of(reservaGuardada));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaCancelada);

        // When
        ReservaResponse resultado = service.cancelar(50L);

        // Then
        assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        verify(horarioClient).cambiarDisponibilidad(10L, true);
    }

    @Test
    @DisplayName("Cancelar reserva inexistente debe lanzar RecursoNoEncontradoException")
    void cancelar_reservaInexistente_lanzaExcepcion() {
        // Given
        when(reservaRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.cancelar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ---- Tests para listarTodos() ----

    @Test
    @DisplayName("Listar todas las reservas debe retornar lista completa")
    void listarTodos_retornaTodasLasReservas() {
        // Given
        when(reservaRepository.findAll()).thenReturn(List.of(reservaGuardada));

        // When
        List<ReservaResponse> resultado = service.listarTodos();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdCliente()).isEqualTo(1L);
    }

    // ---- Tests para listarPorCliente() ----

    @Test
    @DisplayName("Listar por cliente debe retornar solo reservas del cliente")
    void listarPorCliente_retornaReservasDelCliente() {
        // Given
        when(reservaRepository.findByIdCliente(1L)).thenReturn(List.of(reservaGuardada));

        // When
        List<ReservaResponse> resultado = service.listarPorCliente(1L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdCliente()).isEqualTo(1L);
    }
}
