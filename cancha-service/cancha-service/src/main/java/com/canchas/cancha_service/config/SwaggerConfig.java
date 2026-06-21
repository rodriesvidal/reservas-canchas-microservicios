package com.canchas.cancha_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI canchaServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cancha Service API")
                        .description("Microservicio para la gestión de canchas deportivas")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Canchas Deportivas")
                                .email("contacto@canchas.com")));
    }
}
