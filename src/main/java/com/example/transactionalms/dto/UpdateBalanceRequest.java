package com.example.transactionalms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class UpdateBalanceRequest {
    private BigDecimal newBalance;
}
