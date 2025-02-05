package com.example.transactionalms.service;

import com.example.transactionalms.config.token.RequestTokenHolder;
import com.example.transactionalms.dto.AccountDTO;
import com.example.transactionalms.dto.TransactionRequestDTO;
import com.example.transactionalms.dto.TransactionResponseDTO;
import com.example.transactionalms.dto.UpdateBalanceRequest;
import com.example.transactionalms.model.Transaction;
import com.example.transactionalms.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private final WebClient webClient;
    private final TransactionRepository transactionRepository;
    private final RequestTokenHolder tokenHolder;

    public TransactionService(
            TransactionRepository transactionRepository,
            WebClient.Builder webClientBuilder,
            RequestTokenHolder tokenHolder) {
        this.transactionRepository = transactionRepository;
        this.tokenHolder = tokenHolder;
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8080/api/accounts")
                .build();
    }

    public Mono<TransactionResponseDTO> performTransaction(TransactionRequestDTO request) {
        return tokenHolder.getToken()
                .flatMap(token -> {
                    log.info("Using token for transaction: {}", token);
                    return webClient.get()
                            .uri("/{id}", request.getAccountId())
                            .headers(headers -> headers.setBearerAuth(token))
                            .retrieve()
                            .bodyToMono(AccountDTO.class)
                            .flatMap(account -> processTransaction(request, account, token));
                });
    }

    private Mono<TransactionResponseDTO> processTransaction(
            TransactionRequestDTO request,
            AccountDTO account,
            String token) {
        BigDecimal initialBalance = account.getBalance();
        BigDecimal finalBalance;

        if ("DEPOSIT".equalsIgnoreCase(request.getTransactionType())) {
            finalBalance = initialBalance.add(request.getAmount());
        } else if ("WITHDRAWAL".equalsIgnoreCase(request.getTransactionType())) {
            if (request.getAmount().compareTo(initialBalance) > 0) {
                return Mono.error(new IllegalArgumentException("Insufficient balance"));
            }
            finalBalance = initialBalance.subtract(request.getAmount());
        } else {
            return Mono.error(new IllegalArgumentException("Invalid transaction type"));
        }

        Transaction transaction = mapDataToTransaction(request, initialBalance, finalBalance);
        return transactionRepository.save(transaction)
                .flatMap(savedTransaction -> webClient.post()
                        .uri("/{id}/balance", request.getAccountId())
                        .headers(headers -> headers.setBearerAuth(token))
                        .bodyValue(new UpdateBalanceRequest(finalBalance))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .thenReturn(mapToResponse(savedTransaction)));
    }

    private static Transaction mapDataToTransaction(TransactionRequestDTO request, BigDecimal initialBalance, BigDecimal finalBalance) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(request.getAccountId());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setInitialBalance(initialBalance);
        transaction.setAmount(request.getAmount());
        transaction.setFinalBalance(finalBalance);
        transaction.setUserId(request.getUserId());
        transaction.setTimestamp(LocalDateTime.now());
        return transaction;
    }

    private TransactionResponseDTO mapToResponse(Transaction transaction) {
        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setTransactionId(transaction.getId());
        response.setAccountId(transaction.getAccountId());
        response.setTransactionType(transaction.getTransactionType());
        response.setInitialBalance(transaction.getInitialBalance());
        response.setAmount(transaction.getAmount());
        response.setFinalBalance(transaction.getFinalBalance());
        response.setStatus("SUCCESS");
        return response;
    }

    public Flux<Transaction> streamTransactions(String accountId) {
        return transactionRepository.findWithTailableCursorByAccountId(accountId);
    }

    public Mono<Boolean> existsByAccountId(String accountId) {
        return transactionRepository.existsByAccountId(accountId);
    }
}

