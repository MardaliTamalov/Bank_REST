package com.example.bankcards.dto;

import com.example.bankcards.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDto(Transaction transaction) {
}
