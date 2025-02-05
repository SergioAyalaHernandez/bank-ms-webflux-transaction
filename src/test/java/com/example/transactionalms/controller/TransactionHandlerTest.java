package com.example.transactionalms.controller;

import com.example.transactionalms.dto.TransactionRequestDTO;
import com.example.transactionalms.dto.TransactionResponseDTO;
import com.example.transactionalms.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHandlerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionHandler transactionHandler;

    @Test
    void testPerformTransaction_Success() {
        // Arrange
        TransactionRequestDTO requestDTO = new TransactionRequestDTO("1", "DEPOSIT", new BigDecimal("100.00"), "1");
        TransactionResponseDTO responseDTO = new TransactionResponseDTO(); // Configurar como sea necesario

        when(transactionService.performTransaction(any(TransactionRequestDTO.class)))
                .thenReturn(Mono.just(responseDTO));

        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(TransactionRequestDTO.class)).thenReturn(Mono.just(requestDTO));

        // Act
        Mono<ServerResponse> result = transactionHandler.performTransaction(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(serverResponse -> serverResponse.statusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    void testPerformTransaction_Error() {
        // Arrange
        TransactionRequestDTO requestDTO = new TransactionRequestDTO("1", "WITHDRAWAL", new BigDecimal("200.00"), "user123");

        when(transactionService.performTransaction(any(TransactionRequestDTO.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("Insufficient balance for the withdrawal")));

        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(TransactionRequestDTO.class)).thenReturn(Mono.just(requestDTO));

        // Act
        Mono<ServerResponse> result = transactionHandler.performTransaction(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(serverResponse -> serverResponse.statusCode().is4xxClientError())
                .verifyComplete();
    }


}
