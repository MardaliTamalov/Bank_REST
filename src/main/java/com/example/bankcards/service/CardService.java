package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CardService {

    CardResponseDto createCard(CardRequestDto cardRequestDto);

    CardResponseDto getCardById(Long id);

    Page<CardSearchResponseDto> getUserCards(Long userId, CardSearchRequestDto request, Pageable pageable);

    BigDecimal getCardBalance(Long cardId);

    CardResponseDto changeCardStatus(Long cardId, StatusChangeRequestDto newStatus);

    void deleteById(Long id);
}
