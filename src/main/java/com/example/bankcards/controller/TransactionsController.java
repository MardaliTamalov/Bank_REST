package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionRequestDto;
import com.example.bankcards.dto.TransactionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TransactionsController {

    ResponseEntity<TransactionResponseDto> addTransfer(TransactionRequestDto transactionRequestDto);

    @Operation(
            summary = "Получить все транзакции по карте",
            description = "Возвращает входящие и исходящие переводы по карте текущего пользователя"
    )
    @ApiResponse(responseCode = "200", description = "Список транзакций успешно получен")
    ResponseEntity<List<TransactionResponseDto>> getCardTransactions(String cardNumber);
}
