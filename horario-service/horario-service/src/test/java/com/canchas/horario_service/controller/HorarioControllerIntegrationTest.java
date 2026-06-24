package com.canchas.horario_service.controller;

import com.canchas.horario_service.dto.HorarioRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración: Controller -> Service -> Repository contra H2 real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HorarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private HorarioRequest crearRequestValido() {
        HorarioRequest request = new HorarioRequest();
        request.setIdCancha(1L);
        request.setFecha(LocalDate.of(2026, 7, 15));
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setHoraFin(LocalTime.of(11, 0));
        return request;
    }

    @Test
    void crear_devuelve201YDisponibleVerdadero() throws Exception {
        mockMvc.perform(post("/horarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    void crear_conHoraFinAnteriorAHoraInicio_devuelve400() throws Exception {
        HorarioRequest request = crearRequestValido();
        request.setHoraInicio(LocalTime.of(15, 0));
        request.setHoraFin(LocalTime.of(14, 0));

        mockMvc.perform(post("/horarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conHoraFinIgualAHoraInicio_devuelve400() throws Exception {
        HorarioRequest request = crearRequestValido();
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setHoraFin(LocalTime.of(10, 0));

        mockMvc.perform(post("/horarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarDisponibilidad_marcaComoNoDisponible() throws Exception {
        String response = mockMvc.perform(post("/horarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/horarios/{id}/disponibilidad", id).param("disponible", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));
    }

    @Test
    void listarDisponiblesPorCancha_excluyeLosNoDisponibles() throws Exception {
        String response = mockMvc.perform(post("/horarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();
        mockMvc.perform(put("/horarios/{id}/disponibilidad", id).param("disponible", "false"));

        mockMvc.perform(get("/horarios/cancha/{idCancha}/disponibles", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")]").doesNotExist());
    }

    @Test
    void eliminar_existente_devuelve204YLuegoNoSeEncuentra() throws Exception {
        String response = mockMvc.perform(post("/horarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/horarios/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/horarios/{id}", id))
                .andExpect(status().isNotFound());
    }
}
