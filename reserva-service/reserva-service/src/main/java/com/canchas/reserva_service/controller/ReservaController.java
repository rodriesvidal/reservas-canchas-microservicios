package com.canchas.reserva_service.controller;

import com.canchas.reserva_service.dto.ReservaRequest;
import com.canchas.reserva_service.dto.ReservaResponse;
import com.canchas.reserva_service.service.ReservaService;
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
@RequestMapping("/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "API para la gestión de reservas de canchas deportivas. Orquesta la comunicación con usuario-service, cancha-service, horario-service y pago-service.")
public class ReservaController {

    private final ReservaService reservaService;

    @Operation(summary = "Crear reserva",
            description = "Crea una nueva reserva validando cliente, cancha y horario. Genera automáticamente el pago y marca el horario como no disponible.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente",
                    content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente, cancha u horario no encontrado"),
            @ApiResponse(responseCode = "409", description = "El horario ya está reservado")
    })
    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crear(request));
    }

    @Operation(summary = "Listar reservas", description = "Retorna la lista completa de reservas del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de reservas obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listarTodos() {
        return ResponseEntity.ok(reservaService.listarTodos());
    }

    @Operation(summary = "Obtener reserva por ID", description = "Retorna los datos de una reserva específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtenerPorId(
            @Parameter(description = "ID de la reserva", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @Operation(summary = "Listar reservas por cliente", description = "Retorna todas las reservas asociadas a un cliente específico")
    @ApiResponse(responseCode = "200", description = "Lista de reservas del cliente obtenida exitosamente")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<ReservaResponse>> listarPorCliente(
            @Parameter(description = "ID del cliente", required = true) @PathVariable Long idCliente) {
        return ResponseEntity.ok(reservaService.listarPorCliente(idCliente));
    }

    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva existente y libera el horario para futuras reservas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponse> cancelar(
            @Parameter(description = "ID de la reserva", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(reservaService.cancelar(id));
    }
}
