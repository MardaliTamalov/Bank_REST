package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionRequestDto;
import com.example.bankcards.dto.TransactionResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, LiquibaseAutoConfiguration.class})
public class TransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private CardRepository cardRepository;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private TransactionRequestDto requestDto;
    private TransactionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        String fromCard = "1234567890123456";
        String toCard = "9876543210987654";
        BigDecimal amount = new BigDecimal("1000.00");

        requestDto = new TransactionRequestDto(fromCard, toCard, amount);

        Card fromCardEntity = new Card();
        fromCardEntity.setCardNumber(fromCard);

        Card toCardEntity = new Card();
        toCardEntity.setCardNumber(toCard);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setFromCard(fromCardEntity);
        transaction.setToCard(toCardEntity);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());

        responseDto = new TransactionResponseDto(transaction);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("POST /transactions - создание перевода при авторизованном пользователе")
    void addTransferShouldCreateTransactionWhenAuthorized() throws Exception {

        Mockito.when(transactionService.createTransfer(any(TransactionRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction.id").value(1L))
                .andExpect(jsonPath("$.transaction.amount").value(1000.00))
                .andExpect(jsonPath("$.transaction.fromCard.cardNumber").value(requestDto.fromCardNumber()))
                .andExpect(jsonPath("$.transaction.toCard.cardNumber").doesNotExist());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("POST /transactions - возврат ошибки при некорректных данных")
    void addTransferShouldReturnBadRequestWhenInvalidData() throws Exception {
        TransactionRequestDto invalidRequest = new TransactionRequestDto(
                "123", // невалидный номер карты
                "456", // невалидный номер карты
                new BigDecimal("-100") // отрицательная сумма
        );

        mockMvc.perform(post("/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transactions - запрет доступа для неавторизованного пользователя")
    void addTransferShouldReturnForbiddenWhenNotAuthenticated() throws Exception {

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    @DisplayName("GET /transactions/{cardNumber} - получение переводов по карте при авторизации")
    void getCardTransactionsShouldReturnTransactionsWhenAuthorized() throws Exception {
        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setFromCard(responseDto.transaction().getToCard());
        transaction2.setToCard(responseDto.transaction().getFromCard());
        transaction2.setAmount(new BigDecimal("50.00"));
        transaction2.setTimestamp(LocalDateTime.now());

        List<TransactionResponseDto> response = List.of(
                new TransactionResponseDto(responseDto.transaction()),
                new TransactionResponseDto(transaction2)
        );

        Mockito.when(transactionService.getTransactionsByCardId(requestDto.fromCardNumber()))
                .thenReturn(response);

        mockMvc.perform(get("/transactions/{cardNumber}", requestDto.fromCardNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transaction.id").value(1L))
                .andExpect(jsonPath("$[0].transaction.amount").value(1000.00))
                .andExpect(jsonPath("$[0].transaction.fromCard.cardNumber").value(requestDto.fromCardNumber()))
                .andExpect(jsonPath("$[1].transaction.id").value(2L))
                .andExpect(jsonPath("$[1].transaction.amount").value(50.00))
               .andExpect(jsonPath("$[1].transaction.toCard.cardNumber").doesNotExist());
    }

    @Test
    @DisplayName("GET /transactions/{cardNumber} - запрет доступа для неавторизованного пользователя")
    void getCardTransactionsShouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {

        mockMvc.perform(get("/transactions/{cardNumber}", requestDto.fromCardNumber()))
                .andExpect(status().isForbidden());
    }
}