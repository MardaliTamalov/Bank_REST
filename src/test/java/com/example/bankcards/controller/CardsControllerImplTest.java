package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")

class CardsControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    private CardRequestDto validCardRequest;
    private CardResponseDto cardResponse;
    private CardSearchResponseDto cardSearchResponse;

    @BeforeEach
    void setUp() {
        validCardRequest = new CardRequestDto(
                "1234567890123456",
                LocalDate.now().plusYears(2),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(1000),
                1L
        );

        Card mockCard = new Card();
        mockCard.setId(1L);
        mockCard.setCardNumber("encryptedNumber");
        mockCard.setExpirationDate(LocalDate.now().plusYears(2));
        mockCard.setStatus(CardStatus.ACTIVE);
        mockCard.setBalance(BigDecimal.valueOf(1000));

        cardResponse = new CardResponseDto(mockCard);
        String cardStatus = CardStatus.ACTIVE.name();
        cardSearchResponse = new CardSearchResponseDto(1L, "1234********3456", cardStatus, BigDecimal.valueOf(1000), LocalDate.now().plusYears(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /cards - добавление новой карты с корректными данными")
    void addCardValidRequestReturnsCreated() throws Exception {
        when(cardService.createCard(any(CardRequestDto.class))).thenReturn(cardResponse);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card.id").value(1L))
                .andExpect(jsonPath("$.card.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /cards - доступ запрещён для неавторизованного пользователя")
    void addCardUnauthorizedUserReturnsForbidden() throws Exception {
        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /cards - возврат ошибки при некорректных данных")
    void addCardInvalidRequestReturnsBadRequest() throws Exception {
        CardRequestDto invalidRequest = new CardRequestDto(
                "123", // некорректный номер карты
                LocalDate.now().minusDays(1), // дата в прошлом
                null, // null статус
                BigDecimal.valueOf(-100), // отрицательный баланс
                null // null ownerId
        );

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("GET /cards/{id} - получение карты по существующему ID")
    void getCardExistingIdReturnsCard() throws Exception {
        when(cardService.getCardById(anyLong())).thenReturn(cardResponse);

        mockMvc.perform(get("/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card.id").value(1L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("GET /cards/{id} - возврат ошибки при несуществующем ID")
    void getCardNonExistingIdReturnsNotFound() throws Exception {
        when(cardService.getCardById(anyLong())).thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(get("/cards/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("GET /cards/user/{userId} - получение списка карт пользователя")
    void getUserCardsValidRequestReturnsPageOfCards() throws Exception {
        Page<CardSearchResponseDto> page = new PageImpl<>(List.of(cardSearchResponse));
        when(cardService.getUserCards(anyLong(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/cards/user/1")
                        .param("search", "1234")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("1234********3456"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("GET /cards/user/{userId} - возврат пустой страницы при отсутствии карт")
    void getUserCardsNoCardsReturnsEmptyPage() throws Exception {
        Page<CardSearchResponseDto> emptyPage = Page.empty();

        when(cardService.getUserCards(anyLong(), any(CardSearchRequestDto.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/cards/user/1")
                        .param("page", "0")    // обязательный параметр
                        .param("size", "10")  // обязательный параметр
                        .param("sort", "id,desc")) // опционально, если нужно
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("GET /cards/{id}/balance - получение баланса существующей карты")
    void getCardBalanceExistingCardReturnsBalance() throws Exception {
        when(cardService.getCardBalance(anyLong())).thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(get("/cards/1/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /cards/{id}/status - изменение статуса карты")
    void changeCardStatusValidRequestReturnsUpdatedCard() throws Exception {
        StatusChangeRequestDto request = new StatusChangeRequestDto(
                CardStatus.BLOCKED.name());
        Card updatedCard = new Card();
        updatedCard.setId(1L);
        updatedCard.setStatus(CardStatus.BLOCKED);
        CardResponseDto updatedResponse = new CardResponseDto(updatedCard);

        when(cardService.changeCardStatus(anyLong(), any(StatusChangeRequestDto.class))).thenReturn(updatedResponse);

        mockMvc.perform(patch("/cards/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /cards/{id}/status - запрет изменения статуса для неадминистратора")
    void changeCardStatusUnauthorizedUserReturnsForbidden() throws Exception {
        StatusChangeRequestDto request = new StatusChangeRequestDto(
                CardStatus.BLOCKED.name());

        mockMvc.perform(patch("/cards/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /cards/{id} - удаление существующей карты")
    void deleteCardExistingIdReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/cards/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /cards/{id} - запрет удаления карты для неадминистратора")
    void deleteCardUnauthorizedUserReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/cards/1"))
                .andExpect(status().isForbidden());
    }
}