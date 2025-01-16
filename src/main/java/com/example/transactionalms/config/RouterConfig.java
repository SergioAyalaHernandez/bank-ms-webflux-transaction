package com.example.transactionalms.config;

import com.example.transactionalms.controller.TransactionHandler;
import com.example.transactionalms.dto.TransactionRequestDTO;
import com.example.transactionalms.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
public class RouterConfig {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/transactions",
                    method = RequestMethod.POST,
                    beanClass = TransactionHandler.class,
                    beanMethod = "performTransaction",
                    operation = @Operation(
                            operationId = "performTransaction",
                            summary = "Perform a transaction",
                            tags = {"Transaction"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = TransactionRequestDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful operation",
                                            content = @Content(schema = @Schema(implementation = Transaction.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Bad request"
                                    )
                            }
                    )
            )
    })
    public RouterFunction<?> routes(TransactionHandler handler) {
        return RouterFunctions
                .route(POST("/api/transactions"), handler::performTransaction)
                .andRoute(GET("/api/transactions/stream"), handler::streamTransactions);
    }
}