package com.example.bankcards.controller;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User Management", description = "API для управления пользователями")

public interface UsersController {

    @Operation(summary = "Создание нового пользователя", description = "Создает нового пользователя в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<UserResponseDto> addUsers(
            @Parameter(description = "Данные пользователя для создания", required = true)
            UserRequestDto userRequestDto);

    @Operation(summary = "получение пользователя по id", description = " обращение у бд для получения пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "успешно получен пользователь"),
            @ApiResponse(responseCode = "404", description = "not found")
    })
    ResponseEntity<UserResponseDto> getUser(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            Long id);

    @Operation(summary = "Обновление пользователя", description = "Обновление информации о пользователе по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "Обновленные данные пользователя", required = true)
            Long id, UserRequestDto requestDto);

    @Operation(summary = "Удаление пользователя", description = "Удаление пользователя по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            Long id);
}