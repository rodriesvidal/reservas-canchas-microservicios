package com.canchas.reserva_service.service;

import com.canchas.reserva_service.client.CanchaClient;
import com.canchas.reserva_service.client.HorarioClient;
import com.canchas.reserva_service.client.PagoClient;
import com.canchas.reserva_service.client.UsuarioClient;
import com.canchas.reserva_service.client.dto.CanchaDTO;
import com.canchas.reserva_service.client.dto.HorarioDTO;
import com.canchas.reserva_service.client.dto.PagoDTO;
import com.canchas.reserva_service.client.dto.PagoRequestDTO;
import com.canchas.reserva_service.dto.ReservaRequest;
import com.canchas.reserva_service.dto.ReservaResponse;
import com.canchas.reserva_service.exception.HorarioNoDisponibleException;
import com.canchas.reserva_service.exception.RecursoNoEncontradoException;
import com.canchas.reserva_service.model.EstadoReserva;
import com.canchas.reserva_service.model.Reserva;
import com.canchas.reserva_service.repository.ReservaRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioClient usuarioClient;
    private final CanchaClient canchaClient;
    private final HorarioClient horarioClient;
    private final PagoClient pagoClient;

    @Override
    public ReservaResponse crear(ReservaRequest request) {
        // 1. Validar que el cliente exista
        try {
            usuarioClient.obtenerUsuario(request.getIdCliente());
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException(
                    "No existe el cliente con id " + request.getIdCliente());
        }

        // 2. Validar que la cancha exista y obtener su precio
        CanchaDTO cancha;
        try {
            cancha = canchaClient.obtenerCancha(request.getIdCancha());
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException(
                    "No existe la cancha con id " + request.getIdCancha());
        }

        // 3. Validar que el horario exista y esté disponible
        HorarioDTO horario;
        try {
            horario = horarioClient.obtenerHorario(request.getIdHorario());
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException(
                    "No existe el horario con id " + request.getIdHorario());
        }

        if (Boolean.FALSE.equals(horario.getDisponible())) {
            throw new HorarioNoDisponibleException(
                    "El horario con id " + request.getIdHorario() + " ya está reservado");
        }

        // 4. Crear y guardar la reserva
        Reserva reserva = new Reserva();
        reserva.setIdCliente(request.getIdCliente());
        reserva.setIdCancha(request.getIdCancha());
        reserva.setIdHorario(request.getIdHorario());
        reserva.setMonto(cancha.getPrecioHora());
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaReserva(LocalDateTime.now());

        Reserva guardada = reservaRepository.save(reserva);

        // 5. Crear el pago en pago-service
        PagoRequestDTO pagoRequest = new PagoRequestDTO(
                guardada.getId(),
                cancha.getPrecioHora(),
                request.getMetodoPago().name()
        );
        PagoDTO pago = pagoClient.crearPago(pagoRequest);

        // 6. Guardar el id del pago en la reserva
        guardada.setIdPago(pago.getId());
        reservaRepository.save(guardada);

        // 7. Marcar el horario como no disponible
        horarioClient.cambiarDisponibilidad(request.getIdHorario(), false);

        return mapearAResponse(guardada);
    }

    @Override
    public List<ReservaResponse> listarTodos() {
        return reservaRepository.findAll()
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    public ReservaResponse obtenerPorId(Long id) {
        return mapearAResponse(buscarReserva(id));
    }

    @Override
    public List<ReservaResponse> listarPorCliente(Long idCliente) {
        return reservaRepository.findByIdCliente(idCliente)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    public ReservaResponse cancelar(Long id) {
        Reserva reserva = buscarReserva(id);
        reserva.setEstado(EstadoReserva.CANCELADA);
        Reserva actualizada = reservaRepository.save(reserva);

        // liberar el horario para que se pueda volver a reservar
        horarioClient.cambiarDisponibilidad(reserva.getIdHorario(), true);

        return mapearAResponse(actualizada);
    }

    // ---- Métodos privados de apoyo ----

    private Reserva buscarReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la reserva con id " + id));
    }

    private ReservaResponse mapearAResponse(Reserva r) {
        return new ReservaResponse(
                r.getId(),
                r.getIdCliente(),
                r.getIdCancha(),
                r.getIdHorario(),
                r.getMonto(),
                r.getEstado(),
                r.getFechaReserva(),
                r.getIdPago()
        );
    }
}