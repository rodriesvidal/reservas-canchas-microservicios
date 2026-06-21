package com.canchas.reserva_service.exception;

public class HorarioNoDisponibleException extends RuntimeException {

    public HorarioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}