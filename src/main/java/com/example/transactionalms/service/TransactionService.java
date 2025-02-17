package com.example.transactionalms.service;

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
    private final MessagePublisherService messagePublisherService;

    public TransactionService(
            TransactionRepository transactionRepository,
            WebClient.Builder webClientBuilder, MessagePublisherService messagePublisherService) {
        this.transactionRepository = transactionRepository;
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8082/api/accounts")
                .build();
        this.messagePublisherService = messagePublisherService;
    }

    public Mono<TransactionResponseDTO> performTransaction(TransactionRequestDTO request) {
        return webClient.get()
                .uri("/{id}", request.getAccountId())
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .flatMap(account -> processTransaction(request, account));
    }


    private Mono<TransactionResponseDTO> processTransaction(
            TransactionRequestDTO request,
            AccountDTO account) {
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
                        .bodyValue(new UpdateBalanceRequest(finalBalance))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .then(Mono.fromRunnable(() ->
                                messagePublisherService.publishTransactionMessage(
                                        request.getTransactionType(),
                                        request.getAccountId(),
                                        request.getUserId(),
                                        true
                                )
                        ))
                        .thenReturn(mapToResponse(savedTransaction))
                )
                .onErrorResume(e -> {
                    log.error("Transaction failed: {}", e.getMessage());
                    messagePublisherService.publishTransactionMessage(
                            request.getTransactionType(),
                            request.getAccountId(),
                            request.getUserId(),
                            false
                    );
                    return Mono.error(e);
                });
    }

    static Transaction mapDataToTransaction(TransactionRequestDTO request, BigDecimal initialBalance, BigDecimal finalBalance) {
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

