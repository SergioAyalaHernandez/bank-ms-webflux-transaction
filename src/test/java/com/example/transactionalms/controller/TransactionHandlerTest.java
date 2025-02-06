package com.example.transactionalms.controller;

import com.example.transactionalms.dto.TransactionRequestDTO;
import com.example.transactionalms.dto.TransactionResponseDTO;
import com.example.transactionalms.model.Transaction;
import com.example.transactionalms.service.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void testStreamTransactions_MissingAccountId() {
        // Arrange
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam("accountId")).thenReturn(Optional.empty());
        when(request.queryParam("token")).thenReturn(Optional.of("validToken"));

        // Act
        Mono<ServerResponse> result = transactionHandler.streamTransactions(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is4xxClientError())
                .verifyComplete();
    }

    @Test
    void testStreamTransactions_MissingToken() {
        // Arrange
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam("accountId")).thenReturn(Optional.of("123"));
        when(request.queryParam("token")).thenReturn(Optional.empty());

        // Act
        Mono<ServerResponse> result = transactionHandler.streamTransactions(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is4xxClientError())
                .verifyComplete();
    }

    @Test
    void testStreamTransactions_ValidAccountId() {
        // Arrange
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam("accountId")).thenReturn(Optional.of("123"));
        when(request.queryParam("token")).thenReturn(Optional.of("validToken"));

        when(transactionService.existsByAccountId("123")).thenReturn(Mono.just(true));
        when(transactionService.streamTransactions("123")).thenReturn(Flux.just(new Transaction()));

        // Act
        Mono<ServerResponse> result = transactionHandler.streamTransactions(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    void testValidateTransaction_InvalidDTO() {
        // Arrange
        TransactionRequestDTO invalidDTO = new TransactionRequestDTO("", "", BigDecimal.ZERO, "");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionHandler.validateTransaction(invalidDTO);
        });
    }

    @Test
    void testStreamTransactions_NoTransactionsFound() {
        // Arrange
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam("accountId")).thenReturn(Optional.of("123"));
        when(request.queryParam("token")).thenReturn(Optional.of("validToken"));

        when(transactionService.existsByAccountId("123")).thenReturn(Mono.just(false));

        // Act
        Mono<ServerResponse> result = transactionHandler.streamTransactions(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is4xxClientError())
                .verifyComplete();
    }


    @Test
    void testStreamTransactions_ServiceError() {
        // Arrange
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam("accountId")).thenReturn(Optional.of("123"));
        when(request.queryParam("token")).thenReturn(Optional.of("validToken"));

        when(transactionService.existsByAccountId("123")).thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act
        Mono<ServerResponse> result = transactionHandler.streamTransactions(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is5xxServerError())
                .verifyComplete();
    }

    @Test
    void testPerformTransaction_InvalidRequest() {
        // Arrange
        TransactionRequestDTO invalidDTO = new TransactionRequestDTO("", "", BigDecimal.ZERO, "");

        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(TransactionRequestDTO.class)).thenReturn(Mono.just(invalidDTO));

        // Act
        Mono<ServerResponse> result = transactionHandler.performTransaction(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is4xxClientError())
                .verifyComplete();
    }


    @Test
    void testPerformTransaction_Success_2() {
        // Arrange
        TransactionRequestDTO requestDTO = new TransactionRequestDTO("1", "DEPOSIT", new BigDecimal("100.00"), "1");
        TransactionResponseDTO responseDTO = new TransactionResponseDTO();

        when(transactionService.performTransaction(any(TransactionRequestDTO.class)))
                .thenReturn(Mono.just(responseDTO));

        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(TransactionRequestDTO.class)).thenReturn(Mono.just(requestDTO));

        // Act
        Mono<ServerResponse> result = transactionHandler.performTransaction(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();

        // Verifica que el método de servicio se llamó exactamente 1 vez
        verify(transactionService, times(1)).performTransaction(any(TransactionRequestDTO.class));
    }

    @Test
    void testValidateTransaction_Valid() {
        TransactionRequestDTO validDTO = new TransactionRequestDTO("1", "DEPOSIT", new BigDecimal("100.00"), "user123");

        assertDoesNotThrow(() -> {
            transactionHandler.validateTransaction(validDTO);
        });
    }

    @Test
    void testValidateTransaction_Invalid() {
        TransactionRequestDTO invalidDTO = new TransactionRequestDTO("", "", BigDecimal.ZERO, "");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionHandler.validateTransaction(invalidDTO);
        });

        assertTrue(exception.getMessage().contains("Error en los datos de entrada"));
    }

    @Test
    void testPerformTransaction_UnexpectedError() {
        // Arrange
        TransactionRequestDTO requestDTO = new TransactionRequestDTO("1", "DEPOSIT", new BigDecimal("100.00"), "user123");

        when(transactionService.performTransaction(any(TransactionRequestDTO.class)))
                .thenReturn(Mono.error(new RuntimeException("Error interno del servidor")));

        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(TransactionRequestDTO.class)).thenReturn(Mono.just(requestDTO));

        // Act
        Mono<ServerResponse> result = transactionHandler.performTransaction(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is4xxClientError()) // Puede ser 500 dependiendo de cómo manejes errores
                .verifyComplete();
    }


    @Test
    void testStreamTransactions_Success() {
        // Arrange
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam("accountId")).thenReturn(Optional.of("123"));
        when(request.queryParam("token")).thenReturn(Optional.of("validToken"));

        when(transactionService.existsByAccountId("123")).thenReturn(Mono.just(true));
        when(transactionService.streamTransactions("123"))
                .thenReturn(Flux.just(new Transaction()));

        // Act
        Mono<ServerResponse> result = transactionHandler.streamTransactions(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();
    }

}
