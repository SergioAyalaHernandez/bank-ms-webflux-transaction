package com.example.transactionalms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Setter
@Getter
public class AccountDTO {
    private String accountId;
    private BigDecimal balance;
}
