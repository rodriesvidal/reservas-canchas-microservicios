package com.canchas.reserva_service.controller;

import com.canchas.reserva_service.dto.ReservaRequest;
import com.canchas.reserva_service.dto.ReservaResponse;
import com.canchas.reserva_service.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest request) {
        ReservaResponse response = reservaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listarTodos() {
        return ResponseEntity.ok(reservaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<ReservaResponse>> listarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(reservaService.listarPorCliente(idCliente));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.cancelar(id));
    }
}