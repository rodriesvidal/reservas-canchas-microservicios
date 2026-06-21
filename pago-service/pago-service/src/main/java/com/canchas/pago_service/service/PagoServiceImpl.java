package com.canchas.pago_service.service;

import com.canchas.pago_service.dto.PagoRequest;
import com.canchas.pago_service.dto.PagoResponse;
import com.canchas.pago_service.exception.RecursoNoEncontradoException;
import com.canchas.pago_service.model.EstadoPago;
import com.canchas.pago_service.model.Pago;
import com.canchas.pago_service.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;

    @Override
    public PagoResponse crear(PagoRequest request) {
        Pago pago = new Pago();
        pago.setIdReserva(request.getIdReserva());
        pago.setMonto(request.getMonto());
        pago.setMetodoPago(request.getMetodoPago());
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setFechaPago(LocalDateTime.now());

        Pago guardado = pagoRepository.save(pago);
        return mapearAResponse(guardado);
    }

    @Override
    public List<PagoResponse> listarTodos() {
        return pagoRepository.findAll()
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    public PagoResponse obtenerPorId(Long id) {
        return mapearAResponse(buscarPago(id));
    }

    @Override
    public List<PagoResponse> listarPorReserva(Long idReserva) {
        return pagoRepository.findByIdReserva(idReserva)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Override
    public PagoResponse actualizar(Long id, PagoRequest request) {
        Pago pago = buscarPago(id);
        pago.setIdReserva(request.getIdReserva());
        pago.setMonto(request.getMonto());
        pago.setMetodoPago(request.getMetodoPago());
        // estado y fechaPago se mantienen

        Pago actualizado = pagoRepository.save(pago);
        return mapearAResponse(actualizado);
    }

    @Override
    public PagoResponse confirmar(Long id) {
        Pago pago = buscarPago(id);
        pago.setEstado(EstadoPago.PAGADO);
        pago.setFechaPago(LocalDateTime.now());
        return mapearAResponse(pagoRepository.save(pago));
    }

    @Override
    public PagoResponse rechazar(Long id) {
        Pago pago = buscarPago(id);
        pago.setEstado(EstadoPago.RECHAZADO);
        return mapearAResponse(pagoRepository.save(pago));
    }

    @Override
    public void eliminar(Long id) {
        Pago pago = buscarPago(id);
        pagoRepository.delete(pago);
    }

    // ---- Métodos privados de apoyo ----

    private Pago buscarPago(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el pago con id " + id));
    }

    private PagoResponse mapearAResponse(Pago pago) {
        return new PagoResponse(
                pago.getId(),
                pago.getIdReserva(),
                pago.getMonto(),
                pago.getMetodoPago(),
                pago.getEstado(),
                pago.getFechaPago()
        );
    }
}