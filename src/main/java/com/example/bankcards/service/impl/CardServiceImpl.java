package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j

public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Override
    public CardResponseDto createCard(CardRequestDto cardRequestDto) {
        log.info("Creating new card");
        return cardMapper.cardToCardResponseDto(cardRepository.save(cardMapper.requestToCard(cardRequestDto)));
    }

    @Override
    public CardResponseDto getCardById(Long id) {
        log.info("Fetching card with id: {}", id);
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found with id: " + id));
        return cardMapper.cardToCardResponseDto(card);
    }

    @Override
    @Transactional
    public CardResponseDto updateCard(CardRequestDto cardRequestDto) {
        log.info("Updating card with number: {}", cardRequestDto.cardNumber());

        Card existingCard = cardRepository.findByCardNumber(cardRequestDto.cardNumber())
                .orElseThrow(() -> {
                    log.error("Card not found with number: {}", cardRequestDto.cardNumber());
                    return new CardNotFoundException("Card not found with number: " + cardRequestDto.cardNumber());
                });

        // Обновление полей через маппер
        cardMapper.updateCardFromDto(cardRequestDto, existingCard);

        // Сохранение и возврат результата
        Card updatedCard = cardRepository.save(existingCard);
        log.info("Card updated successfully: {}", cardRequestDto.cardNumber());

        return cardMapper.cardToCardResponseDto(updatedCard);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting card with id: {}", id);
        if (cardRepository.existsById(id)) {
            cardRepository.deleteById(id);
        } else throw new CardNotFoundException("Card not found with id: " + id);
    }
}
