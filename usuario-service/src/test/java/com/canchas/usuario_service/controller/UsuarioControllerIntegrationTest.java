package com.canchas.usuario_service.controller;

import com.canchas.usuario_service.dto.UsuarioRequest;
import com.canchas.usuario_service.model.Rol;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración: levanta el contexto completo de Spring (Controller -> Service ->
 * Repository) contra una base de datos H2 real en memoria, sin mocks de por medio.
 * @Transactional revierte los cambios al final de cada test para que no interfieran entre sí.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UsuarioRequest crearRequestValido() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Juan");
        request.setApellido("Pérez");
        request.setEmail("juan.perez@mail.com");
        request.setTelefono("912345678");
        request.setRol(Rol.CLIENTE);
        return request;
    }

    @Test
    void crear_devuelve201YPersisteEnBaseDeDatos() throws Exception {
        UsuarioRequest request = crearRequestValido();

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan.perez@mail.com"));
    }

    @Test
    void crear_sinNombre_devuelve400() throws Exception {
        UsuarioRequest request = crearRequestValido();
        request.setNombre("");

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPorId_existente_devuelveUsuario() throws Exception {
        String response = mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void obtenerPorId_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/usuarios/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listar_devuelveListaConElUsuarioCreado() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRequestValido())));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void actualizar_existente_modificaLosDatos() throws Exception {
        String response = mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        UsuarioRequest actualizacion = crearRequestValido();
        actualizacion.setNombre("Juan Actualizado");

        mockMvc.perform(put("/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Actualizado"));
    }

    @Test
    void eliminar_existente_devuelve204YLuegoNoSeEncuentra() throws Exception {
        String response = mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/usuarios/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/usuarios/{id}", id))
                .andExpect(status().isNotFound());
    }
}
