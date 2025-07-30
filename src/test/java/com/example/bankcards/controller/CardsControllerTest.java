package com.example.bankcards.controller.impl;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardsControllerImpl.class)
public class CardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private CardService cardService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void addCard_ShouldReturnCreatedCard_WhenAdmin() throws Exception {
        // Arrange
        CardRequestDto requestDto = new CardRequestDto(
                "1234567890123456",
                LocalDate.now().plusYears(3),
                CardStatus.ACTIVE,
                BigDecimal.ZERO,
                1L);

        User owner = new User();
        owner.setId(1L);

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("1234567890123456");
        card.setOwner(owner);
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        CardResponseDto responseDto = new CardResponseDto(card);

        Mockito.when(cardService.createCard(any(CardRequestDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card.id").value(1L))
                .andExpect(jsonPath("$.card.cardNumber").value("1234567890123456"))
                .andExpect(jsonPath("$.card.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getCard_ShouldReturnCard_WhenAuthorized() throws Exception {
        // Arrange
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setCardNumber("1234567890123456");
        card.setStatus(CardStatus.ACTIVE);

        CardResponseDto responseDto = new CardResponseDto(card);

        Mockito.when(cardService.getCardById(cardId)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card.id").value(cardId))
                .andExpect(jsonPath("$.card.cardNumber").value("1234567890123456"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getUserCards_ShouldReturnFilteredCards() throws Exception {
        // Arrange
        Long userId = 1L;
        String searchTerm = "1234";
        CardStatus status = CardStatus.ACTIVE;

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("1234567890123456");
        card.setStatus(CardStatus.ACTIVE);

        CardSearchResponseDto responseDto = new CardSearchResponseDto(
                1L, "123456******3456", "ACTIVE", BigDecimal.valueOf(1000), LocalDate.now().plusYears(3));

        Page<CardSearchResponseDto> page = new PageImpl<>(List.of(responseDto),
                PageRequest.of(0, 10, Sort.by("id").descending()), 1);

        Mockito.when(cardService.getUserCards(
                        eq(userId),
                        any(CardSearchRequestDto.class),
                        any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/cards/user/{userId}", userId)
                        .param("search", searchTerm)
                        .param("status", status.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("123456******3456"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getCardBalance_ShouldReturnBalance() throws Exception {
        // Arrange
        Long cardId = 1L;
        BigDecimal balance = BigDecimal.valueOf(1000.50);

        Mockito.when(cardService.getCardBalance(cardId)).thenReturn(balance);

        // Act & Assert
        mockMvc.perform(get("/cards/{cardId}/balance", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.5"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeCardStatus_ShouldUpdateStatus_WhenAdmin() throws Exception {
        // Arrange
        Long cardId = 1L;
        StatusChangeRequestDto request = new StatusChangeRequestDto("BLOCKED");

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);

        CardResponseDto responseDto = new CardResponseDto(card);

        Mockito.when(cardService.changeCardStatus(eq(cardId), any(StatusChangeRequestDto.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(patch("/cards/{cardId}/status", cardId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_ShouldDeleteCard_WhenAdmin() throws Exception {
        // Arrange
        Long cardId = 1L;
        Mockito.doNothing().when(cardService).deleteById(cardId);

        // Act & Assert
        mockMvc.perform(delete("/cards/{id}", cardId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addCard_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        // Arrange
        CardRequestDto requestDto = new CardRequestDto(
                "1234567890123456",
                LocalDate.now().plusYears(3),
                CardStatus.ACTIVE,
                BigDecimal.ZERO,
                1L);

        // Act & Assert
        mockMvc.perform(post("/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }
}