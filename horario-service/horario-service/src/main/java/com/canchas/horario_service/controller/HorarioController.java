package com.canchas.horario_service.controller;

import com.canchas.horario_service.dto.HorarioRequest;
import com.canchas.horario_service.dto.HorarioResponse;
import com.canchas.horario_service.service.HorarioService;
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
@RequestMapping("/horarios")
@RequiredArgsConstructor
@Tag(name = "Horarios", description = "API para la gestión de horarios disponibles de canchas deportivas")
public class HorarioController {

    private final HorarioService horarioService;

    @Operation(summary = "Crear horario", description = "Registra un nuevo horario disponible para una cancha")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Horario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = HorarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o hora de fin anterior a hora de inicio")
    })
    @PostMapping
    public ResponseEntity<HorarioResponse> crear(@Valid @RequestBody HorarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(horarioService.crear(request));
    }

    @Operation(summary = "Listar horarios", description = "Retorna la lista completa de horarios registrados")
    @ApiResponse(responseCode = "200", description = "Lista de horarios obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<HorarioResponse>> listarTodos() {
        return ResponseEntity.ok(horarioService.listarTodos());
    }

    @Operation(summary = "Obtener horario por ID", description = "Retorna los datos de un horario específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Horario encontrado"),
            @ApiResponse(responseCode = "404", description = "Horario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<HorarioResponse> obtenerPorId(
            @Parameter(description = "ID del horario", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(horarioService.obtenerPorId(id));
    }

    @Operation(summary = "Listar horarios por cancha", description = "Retorna todos los horarios de una cancha específica")
    @ApiResponse(responseCode = "200", description = "Lista de horarios de la cancha obtenida exitosamente")
    @GetMapping("/cancha/{idCancha}")
    public ResponseEntity<List<HorarioResponse>> listarPorCancha(
            @Parameter(description = "ID de la cancha", required = true) @PathVariable Long idCancha) {
        return ResponseEntity.ok(horarioService.listarPorCancha(idCancha));
    }

    @Operation(summary = "Listar horarios disponibles por cancha", description = "Retorna únicamente los horarios disponibles para reservar de una cancha")
    @ApiResponse(responseCode = "200", description = "Lista de horarios disponibles obtenida exitosamente")
    @GetMapping("/cancha/{idCancha}/disponibles")
    public ResponseEntity<List<HorarioResponse>> listarDisponiblesPorCancha(
            @Parameter(description = "ID de la cancha", required = true) @PathVariable Long idCancha) {
        return ResponseEntity.ok(horarioService.listarDisponiblesPorCancha(idCancha));
    }

    @Operation(summary = "Actualizar horario", description = "Modifica los datos de un horario existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Horario actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Horario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<HorarioResponse> actualizar(
            @Parameter(description = "ID del horario", required = true) @PathVariable Long id,
            @Valid @RequestBody HorarioRequest request) {
        return ResponseEntity.ok(horarioService.actualizar(id, request));
    }

    @Operation(summary = "Eliminar horario", description = "Elimina un horario del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Horario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Horario no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del horario", required = true) @PathVariable Long id) {
        horarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cambiar disponibilidad", description = "Marca un horario como disponible o no disponible")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Horario no encontrado")
    })
    @PutMapping("/{id}/disponibilidad")
    public ResponseEntity<HorarioResponse> cambiarDisponibilidad(
            @Parameter(description = "ID del horario", required = true) @PathVariable Long id,
            @Parameter(description = "Estado de disponibilidad: true=disponible, false=no disponible", required = true)
            @RequestParam boolean disponible) {
        return ResponseEntity.ok(horarioService.cambiarDisponibilidad(id, disponible));
    }
}
