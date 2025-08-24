package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequestDto(

        @NotBlank
                @Size(min = 16, max = 16)
 String fromCardNumber,

@NotBlank
@Size(min = 16, max = 16)
 String toCardNumber,

        @Positive
       BigDecimal amount

) {
}
