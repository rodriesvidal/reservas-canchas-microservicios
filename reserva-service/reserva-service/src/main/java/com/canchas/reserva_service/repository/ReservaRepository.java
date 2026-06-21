package com.canchas.reserva_service.repository;

import com.canchas.reserva_service.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByIdCliente(Long idCliente);
}