package com.example.bankcards.controller;

import com.example.bankcards.controller.impl.TransactionsControllerImpl;
import com.example.bankcards.dto.TransactionRequestDto;
import com.example.bankcards.dto.TransactionResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionsControllerImpl.class)
public class TransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private TransactionService transactionService;

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void addTransfer_ShouldCreateTransaction_WhenAuthorized() throws Exception {
        // Arrange
        String fromCard = "1234567890123456";
        String toCard = "9876543210987654";
        BigDecimal amount = new BigDecimal("1000.00");

        TransactionRequestDto requestDto = new TransactionRequestDto(fromCard, toCard, amount);

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

        TransactionResponseDto responseDto = new TransactionResponseDto(transaction);

        Mockito.when(transactionService.createTransfer(any(TransactionRequestDto.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction.id").value(1L))
                .andExpect(jsonPath("$.transaction.amount").value(1000.00))
                .andExpect(jsonPath("$.transaction.fromCard.cardNumber").value(fromCard))
                .andExpect(jsonPath("$.transaction.toCard.cardNumber").doesNotExist()); // toCard is @JsonIgnore
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void addTransfer_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // Arrange
        TransactionRequestDto requestDto = new TransactionRequestDto(
                "1234567890123456",
                "9876543210987654",
                new BigDecimal("500.00"));

        // Act & Assert
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getCardTransactions_ShouldReturnTransactions_WhenAuthorized() throws Exception {
        // Arrange
        String cardNumber = "1234567890123456";

        Card card1 = new Card();
        card1.setCardNumber(cardNumber);

        Card card2 = new Card();
        card2.setCardNumber("9876543210987654");

        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setFromCard(card1);
        transaction1.setToCard(card2);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setTimestamp(LocalDateTime.now());

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setFromCard(card2);
        transaction2.setToCard(card1);
        transaction2.setAmount(new BigDecimal("50.00"));
        transaction2.setTimestamp(LocalDateTime.now());

        List<TransactionResponseDto> response = List.of(
                new TransactionResponseDto(transaction1),
                new TransactionResponseDto(transaction2)
        );

        Mockito.when(transactionService.getTransactionsByCardId(cardNumber))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/transactions/{cardNumber}", cardNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transaction.id").value(1L))
                .andExpect(jsonPath("$[0].transaction.amount").value(100.00))
                .andExpect(jsonPath("$[0].transaction.fromCard.cardNumber").value(cardNumber))
                .andExpect(jsonPath("$[1].transaction.id").value(2L))
                .andExpect(jsonPath("$[1].transaction.amount").value(50.00))
                .andExpect(jsonPath("$[1].transaction.toCard.cardNumber").value(cardNumber));
    }

    @Test
    void getCardTransactions_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Arrange
        String cardNumber = "1234567890123456";

        // Act & Assert
        mockMvc.perform(get("/transactions/{cardNumber}", cardNumber))
                .andExpect(status().isUnauthorized());
    }
}