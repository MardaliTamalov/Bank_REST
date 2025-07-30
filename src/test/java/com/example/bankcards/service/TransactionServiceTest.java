package com.example.bankcards.service;

import com.example.bankcards.dto.TransactionRequestDto;
import com.example.bankcards.dto.TransactionResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void getTransactionsByCardIdShouldReturnTransactions() {
        String cardNumber = "1234567890123456";
        Card card = new Card();
        card.setCardNumber(cardNumber);

        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setFromCard(card);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setTimestamp(LocalDateTime.now());

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setToCard(card);
        transaction2.setAmount(new BigDecimal("50.00"));
        transaction2.setTimestamp(LocalDateTime.now());

        List<Transaction> transactions = List.of(transaction1, transaction2);

        TransactionResponseDto dto1 = new TransactionResponseDto(transaction1);
        TransactionResponseDto dto2 = new TransactionResponseDto(transaction2);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(card));
        when(transactionRepository.findByFromCardOrToCard(card, card)).thenReturn(transactions);
        when(transactionMapper.toDto(transaction1)).thenReturn(dto1);
        when(transactionMapper.toDto(transaction2)).thenReturn(dto2);

        List<TransactionResponseDto> result = transactionService.getTransactionsByCardId(cardNumber);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).transaction().getId());
        assertEquals(2L, result.get(1).transaction().getId());

        verify(cardRepository).findByCardNumber(cardNumber);
        verify(transactionRepository).findByFromCardOrToCard(card, card);
        verify(transactionMapper).toDto(transaction1);
        verify(transactionMapper).toDto(transaction2);
    }

    @Test
    void createTransferShouldSuccessfullyCreateTransfer() {

        String fromCardNumber = "1111222233334444";
        String toCardNumber = "5555666677778888";
        BigDecimal amount = new BigDecimal("500.00");

        Card fromCard = new Card();
        fromCard.setCardNumber(fromCardNumber);
        fromCard.setBalance(new BigDecimal("1000.00"));

        Card toCard = new Card();
        toCard.setCardNumber(toCardNumber);
        toCard.setBalance(new BigDecimal("200.00"));

        TransactionRequestDto requestDto = new TransactionRequestDto(fromCardNumber, toCardNumber, amount);
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());

        when(cardRepository.findByCardNumber(fromCardNumber)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber(toCardNumber)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponseDto result = transactionService.createTransfer(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.transaction().getId());
        assertEquals(fromCardNumber, result.transaction().getFromCard().getCardNumber());
        assertEquals(toCardNumber, result.transaction().getToCard().getCardNumber());
        assertEquals(amount, result.transaction().getAmount());

        verify(cardRepository).findByCardNumber(fromCardNumber);
        verify(cardRepository).findByCardNumber(toCardNumber);
        verify(cardRepository).save(fromCard);
        verify(cardRepository).save(toCard);
        verify(transactionRepository).save(any(Transaction.class));

        assertEquals(new BigDecimal("500.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("700.00"), toCard.getBalance());
    }

    @Test
    void createTransferShouldThrowExceptionWhenSameCard() {

        String cardNumber = "1111222233334444";
        TransactionRequestDto requestDto = new TransactionRequestDto(cardNumber, cardNumber, new BigDecimal("100.00"));


        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransfer(requestDto));
        verifyNoInteractions(cardRepository, transactionRepository);
    }

    @Test
    void createTransferShouldThrowExceptionWhenNegativeAmount() {

        TransactionRequestDto requestDto = new TransactionRequestDto(
                "1111222233334444",
                "5555666677778888",
                new BigDecimal("-100.00"));

        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransfer(requestDto));
        verifyNoInteractions(cardRepository, transactionRepository);
    }

    @Test
    void createTransferShouldThrowExceptionWhenFromCardNotFound() {

        String fromCardNumber = "1111222233334444";
        String toCardNumber = "5555666677778888";
        TransactionRequestDto requestDto = new TransactionRequestDto(fromCardNumber, toCardNumber, new BigDecimal("100.00"));

        when(cardRepository.findByCardNumber(fromCardNumber)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> transactionService.createTransfer(requestDto));
        verify(cardRepository).findByCardNumber(fromCardNumber);
        verify(cardRepository, never()).findByCardNumber(toCardNumber);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void createTransferShouldThrowExceptionWhenInsufficientFunds() {

        String fromCardNumber = "1111222233334444";
        String toCardNumber = "5555666677778888";
        BigDecimal amount = new BigDecimal("1000.00");

        Card fromCard = new Card();
        fromCard.setCardNumber(fromCardNumber);
        fromCard.setBalance(new BigDecimal("500.00"));

        Card toCard = new Card();
        toCard.setCardNumber(toCardNumber);
        toCard.setBalance(new BigDecimal("200.00"));

        TransactionRequestDto requestDto = new TransactionRequestDto(fromCardNumber, toCardNumber, amount);

        when(cardRepository.findByCardNumber(fromCardNumber)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumber(toCardNumber)).thenReturn(Optional.of(toCard));

        assertThrows(InsufficientFundsException.class, () -> transactionService.createTransfer(requestDto));
        verify(cardRepository).findByCardNumber(fromCardNumber);
        verify(cardRepository).findByCardNumber(toCardNumber);
        verifyNoInteractions(transactionRepository);

        assertEquals(new BigDecimal("500.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("200.00"), toCard.getBalance());
    }
}