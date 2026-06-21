package com.canchas.reserva_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI reservaServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reserva Service API")
                        .description("Microservicio orquestador para la gestión de reservas de canchas deportivas. Coordina la comunicación con usuario-service, cancha-service, horario-service y pago-service.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Canchas Deportivas")
                                .email("contacto@canchas.com")));
    }
}
