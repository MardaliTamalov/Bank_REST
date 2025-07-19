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
    public List<TransactionResponseDto> getTransactionsByCardId(String cardNumber) {
        log.info("Получение транзакций для карты: {}", cardNumber);
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

//        if (!card.getOwner().getUsername().equals(username)) {
//            throw new ForbiddenAccessException("Нет доступа к карте");
//        }

        List<TransactionResponseDto> transactions = transactionRepository.findByFromCardOrToCard(card, card)
                .stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());

        log.info("Найдено {} транзакций по карте {}", transactions.size(), cardNumber);
        return transactions;
    }

    @Transactional
    public TransactionResponseDto createTransfer(TransactionRequestDto transactionRequestDto) {
        log.info("Создание перевода: {} -> {} на сумму {}",
                transactionRequestDto.fromCardNumber(), transactionRequestDto.toCardNumber(), transactionRequestDto.amount());

        if (transactionRequestDto.fromCardNumber().equals(transactionRequestDto.toCardNumber())) {
            log.warn("Попытка перевода на ту же карту: {}", transactionRequestDto.fromCardNumber());
            throw new IllegalArgumentException("Нельзя перевести на ту же карту");
        }

        if (transactionRequestDto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Некорректная сумма перевода: {}", transactionRequestDto.amount());
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        // Получение карт
        Card fromCard = cardRepository.findByCardNumber(transactionRequestDto.fromCardNumber())
                .orElseThrow(() -> {
                    log.warn("Карта отправителя не найдена: {}", transactionRequestDto.fromCardNumber());
                    return new CardNotFoundException("Карта отправителя не найдена: " + transactionRequestDto.fromCardNumber());
                });

        Card toCard = cardRepository.findByCardNumber(transactionRequestDto.toCardNumber())
                .orElseThrow(() -> {
                    log.warn("Карта получателя не найдена: {}", transactionRequestDto.toCardNumber());
                    return new CardNotFoundException("Карта получателя не найдена: " + transactionRequestDto.toCardNumber());
                });

        // Проверка баланса
        if (fromCard.getBalance().compareTo(transactionRequestDto.amount()) < 0) {
            log.warn("Недостаточно средств на карте {}: баланс {}, требуется {}",
                    transactionRequestDto.fromCardNumber(), fromCard.getBalance(), transactionRequestDto.amount());
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

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Перевод выполнен успешно: транзакция ID = {}", savedTransaction.getId());

        return new TransactionResponseDto(savedTransaction);
    }
}
