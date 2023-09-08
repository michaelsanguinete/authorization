package br.com.caju.authorization.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().addServersItem(new Server().url("https://authorization-production.up.railway.app"))
                .addServersItem(new Server().url("http://localhost:8080"))
                .info(new Info().title("CAJU Authorization").version("V0")
                        .description("API para autorização de transações")
                        .contact(new Contact().name("Michael Sanguinete")
                                .email("michael.sanguinete@gmail.com")));
    }
}
