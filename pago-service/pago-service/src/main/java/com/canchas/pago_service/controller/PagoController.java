package com.canchas.pago_service.controller;

import com.canchas.pago_service.dto.PagoRequest;
import com.canchas.pago_service.dto.PagoResponse;
import com.canchas.pago_service.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<PagoResponse> crear(@Valid @RequestBody PagoRequest request) {
        PagoResponse response = pagoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @GetMapping("/reserva/{idReserva}")
    public ResponseEntity<List<PagoResponse>> listarPorReserva(@PathVariable Long idReserva) {
        return ResponseEntity.ok(pagoService.listarPorReserva(idReserva));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(pagoService.actualizar(id, request));
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<PagoResponse> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.confirmar(id));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<PagoResponse> rechazar(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.rechazar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}