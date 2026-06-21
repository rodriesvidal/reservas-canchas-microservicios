package com.canchas.reserva_service.client;

import com.canchas.reserva_service.client.dto.CanchaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cancha-service")
public interface CanchaClient {

    @GetMapping("/canchas/{id}")
    CanchaDTO obtenerCancha(@PathVariable Long id);
}