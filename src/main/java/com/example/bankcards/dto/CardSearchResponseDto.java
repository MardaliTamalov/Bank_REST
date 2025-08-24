package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardSearchResponseDto(
        Long id,
        String maskedCardNumber,
        String status,
        BigDecimal balance,
        LocalDate expirationDate
) {}
