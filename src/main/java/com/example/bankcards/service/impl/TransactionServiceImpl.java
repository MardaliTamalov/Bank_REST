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

/**
 * Реализация сервиса для работы с транзакциями.
 * <p>
 * Содержит бизнес-логику получения списка транзакций по карте
 * и создания перевода между картами.
 * </p>
 *
 * <p>Основные функции:</p>
 * <ul>
 *     <li>Поиск всех транзакций, связанных с картой (как отправителя, так и получателя)</li>
 *     <li>Выполнение перевода средств между картами с проверкой баланса и корректности данных</li>
 * </ul>
 *
 * <p>Сервис ведет журналирование операций и выбрасывает бизнес-исключения
 * при ошибочных сценариях (недостаточно средств, карта не найдена, некорректные данные).</p>
 *
 * @author …
 */
@Service
@RequiredArgsConstructor
@Slf4j

public class TransactionServiceImpl implements TransactionService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    /**
     * Получает список транзакций по номеру карты.
     * <p>
     * В выборку попадают транзакции, где карта выступала отправителем
     * или получателем средств.
     * </p>
     *
     * @param cardNumber номер карты
     * @return список транзакций в виде {@link TransactionResponseDto}
     * @throws CardNotFoundException если карта с таким номером не найдена
     */
    @Override
    public List<TransactionResponseDto> getTransactionsByCardId(String cardNumber) {
        log.info("Получение транзакций для карты: {}", cardNumber);
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        List<TransactionResponseDto> transactions = transactionRepository.findByFromCardOrToCard(card, card)
                .stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());

        log.info("Найдено {} транзакций по карте {}", transactions.size(), cardNumber);
        return transactions;
    }

    /**
     * Создает перевод между картами.
     * <p>
     * Проверяет корректность суммы, существование обеих карт, достаточность
     * средств на карте отправителя и предотвращает переводы на ту же карту.
     * </p>
     * <p>
     * В случае успешного перевода баланс списывается с карты отправителя,
     * зачисляется на карту получателя, а транзакция сохраняется в базе.
     * </p>
     *
     * @param transactionRequestDto данные перевода (номер карты отправителя,
     *                              номер карты получателя, сумма перевода)
     * @return {@link TransactionResponseDto} с информацией о совершенной транзакции
     * @throws IllegalArgumentException   если сумма некорректная или перевод на ту же карту
     * @throws CardNotFoundException      если карта отправителя или получателя не найдена
     * @throws InsufficientFundsException если на карте отправителя недостаточно средств
     */
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

        if (fromCard.getBalance().compareTo(transactionRequestDto.amount()) < 0) {
            log.warn("Недостаточно средств на карте {}: баланс {}, требуется {}",
                    transactionRequestDto.fromCardNumber(), fromCard.getBalance(), transactionRequestDto.amount());
            throw new InsufficientFundsException("Недостаточно средств на карте: " + transactionRequestDto.fromCardNumber());
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transactionRequestDto.amount()));
        toCard.setBalance(toCard.getBalance().add(transactionRequestDto.amount()));

             cardRepository.save(fromCard);
        cardRepository.save(toCard);

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
