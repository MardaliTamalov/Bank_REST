package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface CardsController {


     ResponseEntity<CardResponseDto> addCard(CardRequestDto card);

    @Operation(summary = "получение карты по id", description = " обращение в бд для получения карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "успешно получена карта"),
            @ApiResponse(responseCode = "404",description = "not found")
    })
    @Parameter(name = "id", description = "id карты", example = "1")
     ResponseEntity<CardResponseDto>  getCard(Long id);


     ResponseEntity<CardResponseDto>  updateCard(CardRequestDto card);


     ResponseEntity<Void> deleteCard(Long id);
}

