package com.example.transactionalms.config;

import com.example.transactionalms.dto.TransactionRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

class TransactionApiTest {

    private final WebTestClient webClient = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:8081")
            .build();

    @Test
    void shouldNotAllowWithdrawalWithInsufficientFunds() {
        TransactionRequestDTO withdrawalRequest = TransactionRequestDTO.builder()
                .transactionType("WITHDRAWAL")
                .accountId("1")
                .userId("1")
                .amount(BigDecimal.valueOf(3000.0))
                .build();

        webClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(withdrawalRequest), TransactionRequestDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Insufficient balance for the withdrawal");

    }

    @Test
    void shouldInvalidTransactionType() {
        TransactionRequestDTO withdrawalRequest = TransactionRequestDTO.builder()
                .transactionType("datoErroneo")
                .accountId("1")
                .userId("1")
                .amount(BigDecimal.valueOf(3000.0))
                .build();

        webClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(withdrawalRequest), TransactionRequestDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Invalid transaction type");

    }
}
