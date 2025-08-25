package co.com.pragma.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Solicitud de creditos")
                        .version("1.0.0")
                        .description("Documentación de la API de Solicitud de creditos")
                        .contact(new Contact().email("emmanuelcastig@gmail.com")
                                .name("Emanuel Castillo G")));
    }
}