package com.example.transactionalms.service;

import com.example.transactionalms.dto.TransactionRequestDTO;
import com.example.transactionalms.dto.TransactionResponseDTO;
import com.example.transactionalms.model.Transaction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionServiceTest {

    @Test
    public void testMapDataToTransaction() {
        // Preparar el objeto de entrada
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountId("account123");
        request.setTransactionType("DEPOSIT");
        request.setAmount(new BigDecimal("100.00"));
        request.setUserId("user123");

        BigDecimal initialBalance = new BigDecimal("200.00");
        BigDecimal finalBalance = initialBalance.add(request.getAmount());

        // Llamar al método estático
        Transaction transaction = TransactionService.mapDataToTransaction(request, initialBalance, finalBalance);

        // Validar que el objeto Transaction tiene los valores correctos
        assertNotNull(transaction);
        assertEquals("account123", transaction.getAccountId());
        assertEquals("DEPOSIT", transaction.getTransactionType());
        assertEquals(initialBalance, transaction.getInitialBalance());
        assertEquals(request.getAmount(), transaction.getAmount());
        assertEquals(finalBalance, transaction.getFinalBalance());
        assertEquals("user123", transaction.getUserId());
        assertNotNull(transaction.getTimestamp());  // Verifica que el timestamp no es null
    }

    @Test
    public void testMapToResponseManually() {
        // Crear una instancia del objeto Transaction
        Transaction transaction = new Transaction();
        transaction.setId(String.valueOf(1L));
        transaction.setAccountId("account123");
        transaction.setTransactionType("DEPOSIT");
        transaction.setInitialBalance(new BigDecimal("200.00"));
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setFinalBalance(new BigDecimal("300.00"));
        transaction.setUserId("user123");
        transaction.setTimestamp(LocalDateTime.now());

        // Crear manualmente el TransactionResponseDTO
        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setTransactionId(transaction.getId());
        response.setAccountId(transaction.getAccountId());
        response.setTransactionType(transaction.getTransactionType());
        response.setInitialBalance(transaction.getInitialBalance());
        response.setAmount(transaction.getAmount());
        response.setFinalBalance(transaction.getFinalBalance());
        response.setStatus("SUCCESS");

        // Validar que el objeto TransactionResponseDTO tiene los valores correctos
        assertNotNull(response);
        assertEquals(String.valueOf(1L), response.getTransactionId());
        assertEquals("account123", response.getAccountId());
        assertEquals("DEPOSIT", response.getTransactionType());
        assertEquals(new BigDecimal("200.00"), response.getInitialBalance());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(new BigDecimal("300.00"), response.getFinalBalance());
        assertEquals("SUCCESS", response.getStatus());
    }



}







