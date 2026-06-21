package com.canchas.horario_service.controller;

import com.canchas.horario_service.dto.HorarioRequest;
import com.canchas.horario_service.dto.HorarioResponse;
import com.canchas.horario_service.service.HorarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/horarios")
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService horarioService;

    @PostMapping
    public ResponseEntity<HorarioResponse> crear(@Valid @RequestBody HorarioRequest request) {
        HorarioResponse response = horarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<HorarioResponse>> listarTodos() {
        return ResponseEntity.ok(horarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HorarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(horarioService.obtenerPorId(id));
    }

    @GetMapping("/cancha/{idCancha}")
    public ResponseEntity<List<HorarioResponse>> listarPorCancha(@PathVariable Long idCancha) {
        return ResponseEntity.ok(horarioService.listarPorCancha(idCancha));
    }

    @GetMapping("/cancha/{idCancha}/disponibles")
    public ResponseEntity<List<HorarioResponse>> listarDisponiblesPorCancha(@PathVariable Long idCancha) {
        return ResponseEntity.ok(horarioService.listarDisponiblesPorCancha(idCancha));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HorarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody HorarioRequest request) {
        return ResponseEntity.ok(horarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        horarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/disponibilidad")
    public ResponseEntity<HorarioResponse> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam boolean disponible) {
        return ResponseEntity.ok(horarioService.cambiarDisponibilidad(id, disponible));
    }
}