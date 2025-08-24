package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;

public record StatusChangeRequestDto(
        @NotNull(message = "Статус карты обязателен")
        String status) {
}
