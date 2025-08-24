package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CardSearchResponseDto;
import com.example.bankcards.dto.StatusChangeRequestDto;
import com.example.bankcards.entity.enums.CardStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

@Tag(name = "Cards Management", description = "API для управления банковскими картами")

public interface CardsController {

    @Operation(summary = "Создание новой карты", description = "Добавляет новую банковскую карту в систему")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные карты"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")})
    ResponseEntity<CardResponseDto> addCard(
            @Parameter(description = "Данные карты для создания", required = true)
            CardRequestDto card);

    @Operation(summary = "Получение карты по ID", description = "Возвращает информацию о карте по её идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно найдена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")})
    ResponseEntity<CardResponseDto> getCard(
            @Parameter(description = "ID карты", example = "1", required = true)
            Long id);

    @Operation(
            summary = "Поиск карт пользователя",
            description = "Возвращает страницу с картами пользователя с возможностью фильтрации по статусу и поиска")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный поиск карт"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")})
    ResponseEntity<Page<CardSearchResponseDto>> getUserCards(
            @Parameter(description = "ID пользователя", example = "123", required = true)
            Long userId,
            @Parameter(description = "Строка поиска (по номеру/названию карты)", example = "Gold")
            String search,
            @Parameter(description = "Статус карты для фильтрации", example = "ACTIVE")
            CardStatus status,
            @Parameter(description = "Параметры пагинации и сортировки",
                    example = "{\"page\":0,\"size\":10,\"sort\":[\"id,desc\"]}")
            Pageable pageable);

    @Operation(
            summary = "Получение баланса карты",
            description = "Возвращает текущий баланс указанной карты"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно получен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")})
    ResponseEntity<BigDecimal> getCardBalance(
            @Parameter(description = "ID карты", example = "456", required = true)
            Long cardId);

    @Operation(
            summary = "Изменение статуса карты",
            description = "Обновляет статус указанной карты (например, BLOCKED, ACTIVE)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")})
    ResponseEntity<CardResponseDto> changeCardStatus(
            @Parameter(description = "ID карты", example = "456", required = true)
            Long cardId,
            @Parameter(description = "Запрос на изменение статуса", required = true)
            StatusChangeRequestDto request);

    @Operation(summary = "Удаление карты", description = "Удаляет карту по её идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")})
    ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID карты для удаления", example = "1", required = true)
            Long id);
}

