package com.canchas.auth_service.controller;

import com.canchas.auth_service.dto.LoginRequest;
import com.canchas.auth_service.dto.RegistroRequest;
import com.canchas.auth_service.model.Rol;
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
 * Test de integración: Controller -> Service -> Repository contra H2 real.
 * Verifica el flujo completo de registro/login, incluido BCrypt y la generación del JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RegistroRequest crearRegistroValido(String email) {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Juan Cliente");
        request.setEmail(email);
        request.setPassword("clave123");
        request.setRol(Rol.CLIENTE);
        return request;
    }

    @Test
    void registrar_devuelve201YToken() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRegistroValido("juan@mail.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void registrar_emailDuplicado_devuelve409() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRegistroValido("repetido@mail.com"))));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRegistroValido("repetido@mail.com"))))
                .andExpect(status().isConflict());
    }

    @Test
    void registrar_passwordMuyCorta_devuelve400() throws Exception {
        RegistroRequest request = crearRegistroValido("corta@mail.com");
        request.setPassword("123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_credencialesValidas_devuelve200YToken() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRegistroValido("login@mail.com"))));

        LoginRequest login = new LoginRequest();
        login.setEmail("login@mail.com");
        login.setPassword("clave123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_passwordIncorrecta_devuelve401() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRegistroValido("malapass@mail.com"))));

        LoginRequest login = new LoginRequest();
        login.setEmail("malapass@mail.com");
        login.setPassword("incorrecta");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_emailInexistente_devuelve401() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("noexiste@mail.com");
        login.setPassword("clave123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
