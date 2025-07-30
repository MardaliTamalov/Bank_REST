package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequestDto(
        String fromCardNumber,
        String toCardNumber,
        BigDecimal amount
) {
}
