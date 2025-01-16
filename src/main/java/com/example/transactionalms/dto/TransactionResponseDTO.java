package com.example.transactionalms.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransactionResponseDTO {
    private String transactionId;
    private String accountId;
    private String transactionType;
    private BigDecimal initialBalance;
    private BigDecimal amount;
    private BigDecimal finalBalance;
    private String status;
}
