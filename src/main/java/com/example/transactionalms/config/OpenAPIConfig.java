package com.example.transactionalms.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Tu API",
                version = "1.0",
                description = "Descripción de tu API"
        )
)
public class OpenAPIConfig {
}
