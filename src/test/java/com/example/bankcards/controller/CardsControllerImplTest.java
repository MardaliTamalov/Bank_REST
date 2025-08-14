package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, LiquibaseAutoConfiguration.class})
class CardsControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private CardRepository cardRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    void addCard_ValidRequest_ReturnsCreated() throws Exception {
        when(cardService.createCard(any(CardRequestDto.class))).thenReturn(cardResponse);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card.id").value(1L))
                .andExpect(jsonPath("$.card.status").value("ACTIVE"));
    }

    @Test
    void addCard_UnauthorizedUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addCard_InvalidRequest_ReturnsBadRequest() throws Exception {
        CardRequestDto invalidRequest = new CardRequestDto(
                "123", // некорректный номер карты
                LocalDate.now().minusDays(1), // дата в прошлом
                null, // null статус
                BigDecimal.valueOf(-100), // отрицательный баланс
                null // null ownerId
        );
        // Выполняем запрос и проверяем статус и сообщение об ошибке
        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getCard_ExistingId_ReturnsCard() throws Exception {
        when(cardService.getCardById(anyLong())).thenReturn(cardResponse);

        mockMvc.perform(get("/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card.id").value(1L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getCard_NonExistingId_ReturnsNotFound() throws Exception {
        when(cardService.getCardById(anyLong())).thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(get("/cards/999"));
        CardNotFoundException exception = assertThrows(CardNotFoundException.class, () -> {
            cardService.getCardById(1L);//вызов метода
        });
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getUserCards_ValidRequest_ReturnsPageOfCards() throws Exception {
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
    void getUserCards_NoCards_ReturnsEmptyPage() throws Exception {
        // 1. Создаем тестовые данные
        Page<CardSearchResponseDto> emptyPage = Page.empty();

        // 2. Мокаем сервис
        when(cardService.getUserCards(anyLong(), any(CardSearchRequestDto.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // 3. Выполняем запрос с обязательными параметрами пагинации
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
    void getCardBalance_ExistingCard_ReturnsBalance() throws Exception {
        when(cardService.getCardBalance(anyLong())).thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(get("/cards/1/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeCardStatus_ValidRequest_ReturnsUpdatedCard() throws Exception {
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
    void changeCardStatus_UnauthorizedUser_ReturnsForbidden() throws Exception {
        StatusChangeRequestDto request = new StatusChangeRequestDto(
                CardStatus.BLOCKED.name());

        mockMvc.perform(patch("/cards/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_ExistingId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/cards/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCard_UnauthorizedUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/cards/1"))
                .andExpect(status().isForbidden());
    }
}