package com.example.transactionalms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequestDTO {
    @NotBlank(message = "El accountId no puede estar vacío.")
    private String accountId;

    @NotBlank(message = "El tipo de transacción no puede estar vacío.")
    private String transactionType; // DEPOSIT or WITHDRAWAL

    @NotNull(message = "El monto no puede ser nulo.")
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser mayor a cero.")
    private BigDecimal amount;

    @NotBlank(message = "El userId no puede estar vacío.")
    private String userId;
}
