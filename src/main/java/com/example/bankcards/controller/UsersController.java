package com.example.bankcards.controller;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface UsersController {


    ResponseEntity<UserResponseDto> addUsers(UserRequestDto userRequestDto);

    @Operation(summary = "получение пользователя по id", description = " обращение у бд для получения пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "успешно получен пользователь"),
            @ApiResponse(responseCode = "404", description = "not found")
    })
    @Parameter(name = "id", description = "id пользователя", example = "1")
    ResponseEntity<UserResponseDto> getUser(Long id) throws InterruptedException;

    ResponseEntity<UserResponseDto> updateUser(Long id, UserRequestDto requestDto);


    ResponseEntity<Void> deleteUser(Long id);
}