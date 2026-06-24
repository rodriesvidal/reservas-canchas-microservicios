package com.canchas.pago_service.controller;

import com.canchas.pago_service.dto.PagoRequest;
import com.canchas.pago_service.model.MetodoPago;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración: Controller -> Service -> Repository contra H2 real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PagoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private PagoRequest crearRequestValido() {
        PagoRequest request = new PagoRequest();
        request.setIdReserva(1L);
        request.setMonto(new BigDecimal("15000"));
        request.setMetodoPago(MetodoPago.EFECTIVO);
        return request;
    }

    @Test
    void crear_devuelve201YEstadoPendiente() throws Exception {
        mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void crear_sinMonto_devuelve400() throws Exception {
        PagoRequest request = crearRequestValido();
        request.setMonto(null);

        mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmar_cambiaEstadoAPagado() throws Exception {
        String response = mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/pagos/{id}/confirmar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADO"));
    }

    @Test
    void rechazar_cambiaEstadoARechazado() throws Exception {
        String response = mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/pagos/{id}/rechazar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADO"));
    }

    @Test
    void obtenerPorId_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/pagos/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorReserva_devuelveSoloLosDeEsaReserva() throws Exception {
        PagoRequest deOtraReserva = crearRequestValido();
        deOtraReserva.setIdReserva(2L);

        mockMvc.perform(post("/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRequestValido())));
        mockMvc.perform(post("/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deOtraReserva)));

        mockMvc.perform(get("/pagos/reserva/{idReserva}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idReserva == 2)]").doesNotExist());
    }

    @Test
    void eliminar_existente_devuelve204YLuegoNoSeEncuentra() throws Exception {
        String response = mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/pagos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/pagos/{id}", id))
                .andExpect(status().isNotFound());
    }
}
