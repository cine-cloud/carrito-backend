package com.unrn.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Carrito de Películas API")
                        .version("1.0.0")
                        .description("API para gestión de carritos de compras de películas"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("carrito-api")
                .pathsToMatch("/carrito/**")
                .packagesToScan("com.unrn.controller")
                .build();
    }
}

