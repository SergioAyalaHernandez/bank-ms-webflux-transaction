package com.example.transactionalms.controller;

import com.example.transactionalms.dto.TransactionRequestDTO;
import com.example.transactionalms.model.Transaction;
import com.example.transactionalms.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class TransactionHandler {

    private final TransactionService transactionService;


    public Mono<ServerResponse> performTransaction(ServerRequest request) {
        return request.bodyToMono(TransactionRequestDTO.class)
                .doOnNext(this::validateTransaction) // Validar DTO
                .flatMap(transactionService::performTransaction)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> streamTransactions(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(transactionService.streamTransactions(), Transaction.class);
    }

    private void validateTransaction(TransactionRequestDTO transactionRequest) {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(transactionRequest);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                    .orElse("Error en los datos de entrada.");
            throw new IllegalArgumentException(errors);
        }
    }

}
