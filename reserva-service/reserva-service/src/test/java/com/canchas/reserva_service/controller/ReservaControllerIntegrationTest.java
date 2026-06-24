package com.canchas.reserva_service.controller;

import com.canchas.reserva_service.client.CanchaClient;
import com.canchas.reserva_service.client.HorarioClient;
import com.canchas.reserva_service.client.PagoClient;
import com.canchas.reserva_service.client.UsuarioClient;
import com.canchas.reserva_service.client.dto.CanchaDTO;
import com.canchas.reserva_service.client.dto.HorarioDTO;
import com.canchas.reserva_service.client.dto.PagoDTO;
import com.canchas.reserva_service.client.dto.UsuarioDTO;
import com.canchas.reserva_service.dto.ReservaRequest;
import com.canchas.reserva_service.model.MetodoPago;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración de reserva-service: Controller -> Service -> Repository contra H2 real.
 * Los 4 clientes Feign se simulan con @MockitoBean porque los demás microservicios
 * no están disponibles durante la ejecución de este módulo aislado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioClient usuarioClient;

    @MockitoBean
    private CanchaClient canchaClient;

    @MockitoBean
    private HorarioClient horarioClient;

    @MockitoBean
    private PagoClient pagoClient;

    private ReservaRequest crearRequestValido() {
        ReservaRequest request = new ReservaRequest();
        request.setIdCliente(1L);
        request.setIdCancha(1L);
        request.setIdHorario(1L);
        request.setMetodoPago(MetodoPago.EFECTIVO);
        return request;
    }

    private void mockearDependenciasFelices() {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        usuario.setNombre("Juan Cliente");
        when(usuarioClient.obtenerUsuario(1L)).thenReturn(usuario);

        CanchaDTO cancha = new CanchaDTO();
        cancha.setId(1L);
        cancha.setNombre("Cancha Norte");
        cancha.setPrecioHora(new BigDecimal("15000"));
        when(canchaClient.obtenerCancha(1L)).thenReturn(cancha);

        HorarioDTO horario = new HorarioDTO();
        horario.setId(1L);
        horario.setIdCancha(1L);
        horario.setDisponible(true);
        when(horarioClient.obtenerHorario(1L)).thenReturn(horario);

        PagoDTO pago = new PagoDTO();
        pago.setId(99L);
        pago.setIdReserva(1L);
        pago.setMonto(new BigDecimal("15000"));
        pago.setEstado("PENDIENTE");
        when(pagoClient.crearPago(any())).thenReturn(pago);

        when(horarioClient.cambiarDisponibilidad(eq(1L), anyBoolean())).thenReturn(horario);
    }

    @Test
    void crear_valida_devuelve201YEstadoConfirmada() throws Exception {
        mockearDependenciasFelices();

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$.monto").value(15000))
                .andExpect(jsonPath("$.idPago").value(99));

        verify(horarioClient).cambiarDisponibilidad(1L, false);
    }

    @Test
    void crear_clienteInexistente_devuelve404() throws Exception {
        when(usuarioClient.obtenerUsuario(1L)).thenThrow(mock(FeignException.NotFound.class));

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_horarioNoDisponible_devuelve409() throws Exception {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        when(usuarioClient.obtenerUsuario(1L)).thenReturn(usuario);

        CanchaDTO cancha = new CanchaDTO();
        cancha.setId(1L);
        cancha.setPrecioHora(new BigDecimal("15000"));
        when(canchaClient.obtenerCancha(1L)).thenReturn(cancha);

        HorarioDTO horarioOcupado = new HorarioDTO();
        horarioOcupado.setId(1L);
        horarioOcupado.setDisponible(false);
        when(horarioClient.obtenerHorario(1L)).thenReturn(horarioOcupado);

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelar_cambiaEstadoACanceladaYLiberaHorario() throws Exception {
        mockearDependenciasFelices();

        String response = mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequestValido())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/reservas/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        verify(horarioClient).cambiarDisponibilidad(1L, true);
    }

    @Test
    void obtenerPorId_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/reservas/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorCliente_devuelveSoloLasDeEseCliente() throws Exception {
        mockearDependenciasFelices();
        mockMvc.perform(post("/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRequestValido())));

        mockMvc.perform(get("/reservas/cliente/{idCliente}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCliente").value(1));
    }
}
