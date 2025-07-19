package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Card Management", description = "API для управления банковскими картами")

public interface CardsController {

    @Operation(summary = "Создание новой карты", description = "Добавляет новую банковскую карту в систему")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные карты"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<CardResponseDto> addCard(
            @Parameter(description = "Данные карты для создания", required = true)
            CardRequestDto card);

    @Operation(summary = "Получение карты по ID", description = "Возвращает информацию о карте по её идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно найдена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    ResponseEntity<CardResponseDto> getCard(
            @Parameter(description = "ID карты", example = "1", required = true)
            Long id);

    @Operation(summary = "Обновление карты", description = "Обновляет информацию о существующей карте")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные карты"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    ResponseEntity<CardResponseDto> updateCard(
            @Parameter(description = "Обновленные данные карты", required = true)
            CardRequestDto card);

    @Operation(summary = "Удаление карты", description = "Удаляет карту по её идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID карты для удаления", example = "1", required = true)
            Long id);
}

