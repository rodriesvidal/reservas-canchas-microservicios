package com.canchas.cancha_service.controller;

import com.canchas.cancha_service.dto.CanchaRequest;
import com.canchas.cancha_service.dto.CanchaResponse;
import com.canchas.cancha_service.service.CanchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/canchas")
@RequiredArgsConstructor
@Tag(name = "Canchas", description = "API para la gestión de canchas deportivas")
public class CanchaController {

    private final CanchaService service;

    @Operation(summary = "Crear cancha", description = "Registra una nueva cancha deportiva en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cancha creada exitosamente",
                    content = @Content(schema = @Schema(implementation = CanchaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<CanchaResponse> crear(@Valid @RequestBody CanchaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @Operation(summary = "Listar canchas", description = "Retorna la lista completa de canchas disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de canchas obtenida exitosamente")
    @GetMapping
    public List<CanchaResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Obtener cancha por ID", description = "Retorna los datos de una cancha específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancha encontrada",
                    content = @Content(schema = @Schema(implementation = CanchaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cancha no encontrada")
    })
    @GetMapping("/{id}")
    public CanchaResponse obtener(
            @Parameter(description = "ID de la cancha", required = true) @PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @Operation(summary = "Listar canchas por propietario", description = "Retorna todas las canchas de un propietario específico")
    @ApiResponse(responseCode = "200", description = "Lista de canchas del propietario obtenida exitosamente")
    @GetMapping("/propietario/{idPropietario}")
    public List<CanchaResponse> porPropietario(
            @Parameter(description = "ID del propietario", required = true) @PathVariable Long idPropietario) {
        return service.listarPorPropietario(idPropietario);
    }

    @Operation(summary = "Actualizar cancha", description = "Modifica los datos de una cancha existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancha actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cancha no encontrada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PutMapping("/{id}")
    public CanchaResponse actualizar(
            @Parameter(description = "ID de la cancha", required = true) @PathVariable Long id,
            @Valid @RequestBody CanchaRequest req) {
        return service.actualizar(id, req);
    }

    @Operation(summary = "Eliminar cancha", description = "Elimina una cancha del sistema por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cancha eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cancha no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la cancha", required = true) @PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
