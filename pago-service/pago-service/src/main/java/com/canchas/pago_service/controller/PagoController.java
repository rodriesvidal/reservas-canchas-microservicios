package com.canchas.pago_service.controller;

import com.canchas.pago_service.dto.PagoRequest;
import com.canchas.pago_service.dto.PagoResponse;
import com.canchas.pago_service.service.PagoService;
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
@RequestMapping("/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "API para el procesamiento y gestión de pagos de reservas deportivas")
public class PagoController {

    private final PagoService pagoService;

    @Operation(summary = "Crear pago", description = "Registra un nuevo pago asociado a una reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago creado exitosamente en estado PENDIENTE",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<PagoResponse> crear(@Valid @RequestBody PagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.crear(request));
    }

    @Operation(summary = "Listar pagos", description = "Retorna la lista completa de pagos registrados")
    @ApiResponse(responseCode = "200", description = "Lista de pagos obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<PagoResponse>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @Operation(summary = "Obtener pago por ID", description = "Retorna los datos de un pago específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> obtenerPorId(
            @Parameter(description = "ID del pago", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @Operation(summary = "Listar pagos por reserva", description = "Retorna todos los pagos asociados a una reserva")
    @ApiResponse(responseCode = "200", description = "Lista de pagos de la reserva obtenida exitosamente")
    @GetMapping("/reserva/{idReserva}")
    public ResponseEntity<List<PagoResponse>> listarPorReserva(
            @Parameter(description = "ID de la reserva", required = true) @PathVariable Long idReserva) {
        return ResponseEntity.ok(pagoService.listarPorReserva(idReserva));
    }

    @Operation(summary = "Actualizar pago", description = "Modifica los datos de un pago existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PagoResponse> actualizar(
            @Parameter(description = "ID del pago", required = true) @PathVariable Long id,
            @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(pagoService.actualizar(id, request));
    }

    @Operation(summary = "Confirmar pago", description = "Cambia el estado del pago a PAGADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago confirmado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<PagoResponse> confirmar(
            @Parameter(description = "ID del pago", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(pagoService.confirmar(id));
    }

    @Operation(summary = "Rechazar pago", description = "Cambia el estado del pago a RECHAZADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago rechazado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<PagoResponse> rechazar(
            @Parameter(description = "ID del pago", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(pagoService.rechazar(id));
    }

    @Operation(summary = "Eliminar pago", description = "Elimina un pago del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pago eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del pago", required = true) @PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
