package com.canchas.pago_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI pagoServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pago Service API")
                        .description("Microservicio para el procesamiento y gestión de pagos de reservas")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Canchas Deportivas")
                                .email("contacto@canchas.com")));
    }
}
