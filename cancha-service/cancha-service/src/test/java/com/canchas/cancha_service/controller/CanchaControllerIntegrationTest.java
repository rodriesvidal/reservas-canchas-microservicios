package com.canchas.cancha_service.controller;

import com.canchas.cancha_service.dto.CanchaRequest;
import com.canchas.cancha_service.model.TipoCancha;
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
class CanchaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CanchaRequest crearRequestValido() {
        CanchaRequest request = new CanchaRequest();
        request.setNombre("Cancha Fútbol Norte");
        request.setTipo(TipoCancha.FUTBOL);
        request.setUbicacion("Av. Siempre Viva 123");
        request.setPrecioHora(new BigDecimal("15000"));
        request.setIdPropietario(1L);
        return request;
    }

    @Test
    void crear_devuelve201YEstadoDisponiblePorDefecto() throws Exception {
        mockMvc.perform(post("/canchas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    }

    @Test
    void crear_sinPrecioHora_devuelve400() throws Exception {
        CanchaRequest request = crearRequestValido();
        request.setPrecioHora(null);

        mockMvc.perform(post("/canchas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPorId_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/canchas/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorPropietario_devuelveSoloLasDeEsePropietario() throws Exception {
        CanchaRequest deOtroPropietario = crearRequestValido();
        deOtroPropietario.setIdPropietario(2L);

        mockMvc.perform(post("/canchas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRequestValido())));
        mockMvc.perform(post("/canchas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deOtroPropietario)));

        mockMvc.perform(get("/canchas/propietario/{idPropietario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idPropietario == 2)]").doesNotExist());
    }

    @Test
    void actualizar_existente_modificaLosDatos() throws Exception {
        String response = mockMvc.perform(post("/canchas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        CanchaRequest actualizacion = crearRequestValido();
        actualizacion.setNombre("Cancha Renovada");

        mockMvc.perform(put("/canchas/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cancha Renovada"));
    }

    @Test
    void eliminar_existente_devuelve204YLuegoNoSeEncuentra() throws Exception {
        String response = mockMvc.perform(post("/canchas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/canchas/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/canchas/{id}", id))
                .andExpect(status().isNotFound());
    }
}
