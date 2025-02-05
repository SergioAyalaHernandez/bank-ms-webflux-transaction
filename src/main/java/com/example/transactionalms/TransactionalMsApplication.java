package com.example.transactionalms;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Transactional Microservice API",
                version = "1.0",
                description = "API for handling transactions"
        )
)
public class TransactionalMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionalMsApplication.class, args);
    }

}
