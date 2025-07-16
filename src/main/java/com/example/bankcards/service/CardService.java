package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;

public interface CardService {

    CardResponseDto createCard(CardRequestDto cardRequestDto);

    CardResponseDto getCardById(Long id);

    CardResponseDto updateCard(CardRequestDto cardRequestDto);

    void deleteById(Long id);
}
