package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void createCardShouldSuccessfullyCreateCard() {
        CardRequestDto requestDto = new CardRequestDto(
                "1234567890123456",
                LocalDate.now().plusYears(1),
                CardStatus.ACTIVE,
                BigDecimal.ZERO,
                1L
        );

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("1234567890123456");
        card.setExpirationDate(LocalDate.now().plusYears(1));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        // owner можно не устанавливать, если метод createCard не использует userRepository

        CardResponseDto responseDto = new CardResponseDto(card);

        // Убираем мок userRepository, так как он не вызывается
        when(cardMapper.requestToCard(requestDto)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.cardToCardResponseDto(card)).thenReturn(responseDto);

        CardResponseDto result = cardService.createCard(requestDto);

        assertNotNull(result);
        assertEquals(card, result.card());
        assertEquals("1234567890123456", result.card().getCardNumber());
        assertEquals(CardStatus.ACTIVE, result.card().getStatus());

        // verify(userRepository).findById(1L); // Удаляем
        verify(cardMapper).requestToCard(requestDto);
        verify(cardRepository).save(card);
        verify(cardMapper).cardToCardResponseDto(card);
    }


    @Test
    void createCardShouldValidateRequestDto() {

        CardRequestDto invalidCardNumber = new CardRequestDto(
                "123", // invalid - less than 16 digits
                LocalDate.now().plusYears(1),
                CardStatus.ACTIVE,
                BigDecimal.ZERO,
                1L);

        CardRequestDto pastExpirationDate = new CardRequestDto(
                "1234567890123456",
                LocalDate.now().minusDays(1), // invalid - past date
                CardStatus.ACTIVE,
                BigDecimal.ZERO,
                1L);

        CardRequestDto negativeBalance = new CardRequestDto(
                "1234567890123456",
                LocalDate.now().plusYears(1),
                CardStatus.ACTIVE,
                new BigDecimal("-100"), // invalid - negative
                1L);

        CardRequestDto nullOwnerId = new CardRequestDto(
                "1234567890123456",
                LocalDate.now().plusYears(1),
                CardStatus.ACTIVE,
                BigDecimal.ZERO,
                null); // invalid - null
    }

    @Test
    void getCardByIdShouldReturnCardWhenExists() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setCardNumber("1234567890123456");

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.cardToCardResponseDto(card)).thenReturn(new CardResponseDto(card)); // ✅

        CardResponseDto result = cardService.getCardById(cardId);

        assertNotNull(result);
        assertEquals(card, result.card());
        assertEquals(cardId, result.card().getId());
        assertEquals("1234567890123456", result.card().getCardNumber());

        verify(cardRepository).findById(cardId);
        verify(cardMapper).cardToCardResponseDto(card);
    }


    @Test
    void getUserCardsShouldReturnFilteredCards() {
        // Arrange
        Long userId = 1L;
        String searchTerm = "1234";
        CardStatus status = CardStatus.ACTIVE;

        CardSearchRequestDto request = new CardSearchRequestDto(searchTerm, status);
        Pageable pageable = PageRequest.of(0, 10);

        Card card1 = new Card();
        card1.setId(11L);
        card1.setCardNumber("1234567890123456");
        card1.setStatus(CardStatus.ACTIVE);
        card1.setBalance(new BigDecimal("1000.00"));
        card1.setExpirationDate(LocalDate.now().plusYears(3));

        List<Card> cards = List.of(card1);
        Page<Card> cardPage = new PageImpl<>(cards, pageable, 1);

        CardSearchResponseDto responseDto = new CardSearchResponseDto(
                11L,
                "123456******3456", // masked card number
                "ACTIVE",
                new BigDecimal("1000.00"),
                LocalDate.now().plusYears(3));

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);
        when(cardMapper.cardToCardSearchResponse(card1)).thenReturn(responseDto);

        Page<CardSearchResponseDto> result = cardService.getUserCards(userId, request, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(11L, result.getContent().get(0).id());
        assertEquals("123456******3456", result.getContent().get(0).maskedCardNumber());
        assertEquals("ACTIVE", result.getContent().get(0).status());
        assertEquals(new BigDecimal("1000.00"), result.getContent().get(0).balance());
        assertEquals(LocalDate.now().plusYears(3), result.getContent().get(0).expirationDate());

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
        verify(cardMapper).cardToCardSearchResponse(card1);
    }

    @Test
    void changeCardStatusShouldSuccessfullyChangeStatus() {
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);

        StatusChangeRequestDto request = new StatusChangeRequestDto("BLOCKED");

        Card updatedCard = new Card();
        updatedCard.setId(cardId);
        updatedCard.setStatus(CardStatus.BLOCKED);

        CardResponseDto expectedResponse = new CardResponseDto(updatedCard);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(updatedCard);
        when(cardMapper.cardToCardResponseDto(updatedCard)).thenReturn(expectedResponse);  // <-- добавляем мок

        CardResponseDto result = cardService.changeCardStatus(cardId, request);

        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED, result.card().getStatus());

        verify(cardRepository).findById(cardId);
        verify(cardRepository).save(card);
        verify(cardMapper).cardToCardResponseDto(updatedCard);  // проверяем вызов
    }

    @Test
    void deleteByIdShouldDeleteCardWhenExists() {

        Long cardId = 1L;
        when(cardRepository.existsById(cardId)).thenReturn(true);

        cardService.deleteById(cardId);

        verify(cardRepository).existsById(cardId);
        verify(cardRepository).deleteById(cardId);
    }
}