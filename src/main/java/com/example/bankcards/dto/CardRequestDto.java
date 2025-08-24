package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardRequestDto(
        @NotBlank(message = "Номер карты не может быть пустым")
        @Pattern(regexp = "^[0-9]{16}$", message = "Номер карты должен содержать 16 цифр")
        String cardNumber,

        @NotNull(message = "Дата окончания срока действия обязательна")
        @Future(message = "Дата окончания срока действия должна быть в будущем")
        LocalDate expirationDate,

        @NotNull(message = "Статус карты обязателен")
        CardStatus status,

        @NotNull(message = "Баланс не может быть null")
        @PositiveOrZero(message = "Баланс не может быть отрицательным")
        BigDecimal balance,

         @NotNull(message = "ID владельца обязательно")
                Long ownerId
) {
}
