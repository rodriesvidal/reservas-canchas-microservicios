package com.canchas.reserva_service.client;

import com.canchas.reserva_service.client.dto.PagoDTO;
import com.canchas.reserva_service.client.dto.PagoRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pago-service")
public interface PagoClient {

    @PostMapping("/pagos")
    PagoDTO crearPago(@RequestBody PagoRequestDTO request);
}