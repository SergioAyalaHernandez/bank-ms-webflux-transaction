package com.example.transactionalms.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "transactions")
@Data
public class Transaction {
    @Id
    private String id;
    private String accountId;
    private String transactionType; // DEPOSIT or WITHDRAWAL
    private BigDecimal initialBalance;
    private BigDecimal amount;
    private BigDecimal finalBalance;
    private String userId;
    private LocalDateTime timestamp;
}
