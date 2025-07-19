package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionRequestDto;
import com.example.bankcards.dto.TransactionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Transaction Management", description = "API для управления банковскими транзакциями")

public interface TransactionsController {

    @Operation(summary = "Создание новой транзакции",
            description = "Создает новую банковскую транзакцию (перевод средств)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Транзакция успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные транзакции"),
            @ApiResponse(responseCode = "404", description = "Карта отправителя или получателя не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<TransactionResponseDto> addTransfer(
            @Parameter(description = "Данные для создания транзакции", required = true)
            TransactionRequestDto transactionRequestDto);

    @Operation(summary = "Получение транзакций по карте",
            description = "Возвращает список всех транзакций для указанной карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список транзакций успешно получен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })

    @ApiResponse(responseCode = "200", description = "Список транзакций успешно получен")
    ResponseEntity<List<TransactionResponseDto>> getCardTransactions(
            @Parameter(description = "Номер банковской карты", example = "1234567890123456", required = true)
            String cardNumber);
}
