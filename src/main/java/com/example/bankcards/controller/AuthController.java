package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequestDto;
import com.example.bankcards.dto.AuthResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Аутентификация",
        description = "API для регистрации, входа и получения информации о пользователе")
public interface AuthController {

    @Operation(
            summary = "Аутентификация пользователя",
            description = "Позволяет пользователю войти в систему, используя email и пароль. Возвращает JWT токен для доступа к защищенным ресурсам.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Пример запроса",
                                            value = """
                                                    {
                                                        "username": "user@example.com",
                                                        "password": "Password123!"
                                                    }"""
                                    )})))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Пример ответа",
                                            value = """
                                                    {
                                                        "token": "eyJhbGciOiJIUzI1NiIs...",
                                                        "username": "user@example.com",
                                                        "role": "USER"
                                                    }"""
                                    )})),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные учетные данные",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неудачная аутентификация",
                    content = @Content
            )})
    ResponseEntity<AuthResponseDto> authenticateUser(AuthRequestDto loginRequest);

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с ролью USER. После успешной регистрации автоматически аутентифицирует пользователя и возвращает JWT токен.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Пример запроса",
                                            value = """
                                                    {
                                                        "username": "newuser@example.com",
                                                        "password": "Password123!"
                                                    }"""
                                    )}))
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная регистрация и аутентификация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Пример ответа",
                                            value = """
                                                    {
                                                        "token": "eyJhbGciOiJIUzI1NiIs...",
                                                        "username": "newuser@example.com",
                                                        "role": "USER"
                                                    }"""
                                    )})),
            @ApiResponse(
                    responseCode = "400",
                    description = "Пользователь с таким email уже существует или неверные данные",
                    content = @Content
            )})
    ResponseEntity<AuthResponseDto> registerUser(AuthRequestDto registerRequest);

}