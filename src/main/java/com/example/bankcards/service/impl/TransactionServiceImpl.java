package com.example.bankcards.service.impl;

import com.example.bankcards.dto.TransactionRequestDto;
import com.example.bankcards.dto.TransactionResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class TransactionServiceImpl implements TransactionService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public List<TransactionResponseDto> getTransactionsByCardId(String cardNumber){
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

//        if (!card.getOwner().getUsername().equals(username)) {
//            throw new ForbiddenAccessException("Нет доступа к карте");
//        }

        return transactionRepository.findByFromCardOrToCard(card, card)
                .stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponseDto createTransfer(TransactionRequestDto transactionRequestDto) {
        if (transactionRequestDto.fromCardNumber().equals(transactionRequestDto.toCardNumber())) {
            throw new IllegalArgumentException("Нельзя перевести на ту же карту");
        }

        if (transactionRequestDto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        // Получение карт
        Card fromCard = cardRepository.findByCardNumber(transactionRequestDto.fromCardNumber())
                .orElseThrow(() -> new CardNotFoundException("Карта отправителя не найдена: " + transactionRequestDto.fromCardNumber()));

        Card toCard = cardRepository.findByCardNumber(transactionRequestDto.toCardNumber())
                .orElseThrow(() -> new CardNotFoundException("Карта получателя не найдена: " + transactionRequestDto.toCardNumber()));

        // Проверка баланса
        if (fromCard.getBalance().compareTo(transactionRequestDto.amount()) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на карте: " + transactionRequestDto.fromCardNumber());
        }

        // Списание и зачисление
        fromCard.setBalance(fromCard.getBalance().subtract(transactionRequestDto.amount()));
        toCard.setBalance(toCard.getBalance().add(transactionRequestDto.amount()));

        // Сохранение изменений в базу
        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        // Создание транзакции
        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(transactionRequestDto.amount());
        transaction.setTimestamp(LocalDateTime.now());

        return new TransactionResponseDto(transactionRepository.save(transaction));
    }


}
