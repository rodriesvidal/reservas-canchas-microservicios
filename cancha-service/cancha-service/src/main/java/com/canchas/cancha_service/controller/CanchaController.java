package com.canchas.cancha_service.controller;

import com.canchas.cancha_service.dto.CanchaRequest;
import com.canchas.cancha_service.dto.CanchaResponse;
import com.canchas.cancha_service.service.CanchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/canchas")
@RequiredArgsConstructor
public class CanchaController {

    private final CanchaService service;

    @PostMapping
    public ResponseEntity<CanchaResponse> crear(@Valid @RequestBody CanchaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @GetMapping
    public List<CanchaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CanchaResponse obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @GetMapping("/propietario/{idPropietario}")
    public List<CanchaResponse> porPropietario(@PathVariable Long idPropietario) {
        return service.listarPorPropietario(idPropietario);
    }

    @PutMapping("/{id}")
    public CanchaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CanchaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}