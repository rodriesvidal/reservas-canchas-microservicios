package com.canchas.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<Map<String, Object>> manejarEmailYaRegistrado(
            EmailYaRegistradoException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("estado", HttpStatus.CONFLICT.value());
        error.put("error", "Conflicto");
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> manejarCredencialesInvalidas(
            CredencialesInvalidasException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("estado", HttpStatus.UNAUTHORIZED.value());
        error.put("error", "No autorizado");
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(
            HttpMessageNotReadableException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("estado", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Solicitud inválida");
        error.put("mensaje", "El cuerpo de la solicitud es inválido. " +
                "Revisa que el rol sea CLIENTE, PROPIETARIO o ADMIN.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(
            MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("estado", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", "Error de validación");
        respuesta.put("campos", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }
}