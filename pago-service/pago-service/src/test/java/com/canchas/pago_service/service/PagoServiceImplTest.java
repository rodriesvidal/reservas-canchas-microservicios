package com.canchas.pago_service.service;

import com.canchas.pago_service.dto.PagoRequest;
import com.canchas.pago_service.dto.PagoResponse;
import com.canchas.pago_service.exception.RecursoNoEncontradoException;
import com.canchas.pago_service.model.EstadoPago;
import com.canchas.pago_service.model.MetodoPago;
import com.canchas.pago_service.model.Pago;
import com.canchas.pago_service.repository.PagoRepository;
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
@DisplayName("Tests unitarios de PagoServiceImpl")
class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private PagoServiceImpl service;

    private Pago pagoEjemplo;
    private PagoRequest requestEjemplo;

    @BeforeEach
    void setUp() {
        pagoEjemplo = new Pago(
                1L, 100L,
                new BigDecimal("15000"),
                MetodoPago.TARJETA,
                EstadoPago.PENDIENTE,
                LocalDateTime.now()
        );

        requestEjemplo = new PagoRequest();
        requestEjemplo.setIdReserva(100L);
        requestEjemplo.setMonto(new BigDecimal("15000"));
        requestEjemplo.setMetodoPago(MetodoPago.TARJETA);
    }

    // ---- Tests para crear() ----

    @Test
    @DisplayName("Crear pago debe guardar con estado PENDIENTE")
    void crear_debeGuardarConEstadoPendiente() {
        // Given
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoEjemplo);

        // When
        PagoResponse resultado = service.crear(requestEjemplo);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstado()).isEqualTo(EstadoPago.PENDIENTE);
        assertThat(resultado.getMonto()).isEqualByComparingTo(new BigDecimal("15000"));
        assertThat(resultado.getMetodoPago()).isEqualTo(MetodoPago.TARJETA);
        verify(pagoRepository).save(any(Pago.class));
    }

    @Test
    @DisplayName("Crear pago debe registrar la fecha del pago")
    void crear_debeRegistrarFechaPago() {
        // Given
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoEjemplo);

        // When
        PagoResponse resultado = service.crear(requestEjemplo);

        // Then
        assertThat(resultado.getFechaPago()).isNotNull();
    }

    // ---- Tests para obtenerPorId() ----

    @Test
    @DisplayName("Obtener pago por ID existente debe retornar PagoResponse")
    void obtenerPorId_idExistente_retornaPago() {
        // Given
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo));

        // When
        PagoResponse resultado = service.obtenerPorId(1L);

        // Then
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getIdReserva()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Obtener pago por ID inexistente debe lanzar RecursoNoEncontradoException")
    void obtenerPorId_idInexistente_lanzaExcepcion() {
        // Given
        when(pagoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    // ---- Tests para confirmar() ----

    @Test
    @DisplayName("Confirmar pago debe cambiar estado a PAGADO")
    void confirmar_debeCambiarEstadoAPagado() {
        // Given
        Pago pagoPagado = new Pago(1L, 100L, new BigDecimal("15000"),
                MetodoPago.TARJETA, EstadoPago.PAGADO, LocalDateTime.now());
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoPagado);

        // When
        PagoResponse resultado = service.confirmar(1L);

        // Then
        assertThat(resultado.getEstado()).isEqualTo(EstadoPago.PAGADO);
        verify(pagoRepository).save(any(Pago.class));
    }

    @Test
    @DisplayName("Confirmar pago inexistente debe lanzar RecursoNoEncontradoException")
    void confirmar_pagoInexistente_lanzaExcepcion() {
        // Given
        when(pagoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.confirmar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ---- Tests para rechazar() ----

    @Test
    @DisplayName("Rechazar pago debe cambiar estado a RECHAZADO")
    void rechazar_debeCambiarEstadoARechazado() {
        // Given
        Pago pagoRechazado = new Pago(1L, 100L, new BigDecimal("15000"),
                MetodoPago.TARJETA, EstadoPago.RECHAZADO, LocalDateTime.now());
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoRechazado);

        // When
        PagoResponse resultado = service.rechazar(1L);

        // Then
        assertThat(resultado.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
    }

    // ---- Tests para listarPorReserva() ----

    @Test
    @DisplayName("Listar pagos por reserva debe retornar solo pagos de esa reserva")
    void listarPorReserva_retornaPagosDeLaReserva() {
        // Given
        when(pagoRepository.findByIdReserva(100L)).thenReturn(List.of(pagoEjemplo));

        // When
        List<PagoResponse> resultado = service.listarPorReserva(100L);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdReserva()).isEqualTo(100L);
    }

    // ---- Tests para eliminar() ----

    @Test
    @DisplayName("Eliminar pago existente debe invocar delete en el repositorio")
    void eliminar_pagoExistente_invocaDelete() {
        // Given
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo));

        // When
        service.eliminar(1L);

        // Then
        verify(pagoRepository).delete(pagoEjemplo);
    }

    @Test
    @DisplayName("Eliminar pago inexistente debe lanzar RecursoNoEncontradoException")
    void eliminar_pagoInexistente_lanzaExcepcion() {
        // Given
        when(pagoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(pagoRepository, never()).delete(any());
    }
}
