package com.example.bankcards.service.impl;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Реализация сервиса для управления банковскими картами.
 * <p>
 * Содержит бизнес-логику создания, получения, поиска, изменения статуса и удаления карт.
 * Также выполняет автоматическое обновление статусов просроченных карт по расписанию.
 * </p>
 *
 * <p>Основные операции:</p>
 * <ul>
 *     <li>Создание новой карты</li>
 *     <li>Получение баланса карты</li>
 *     <li>Поиск карты по идентификатору</li>
 *     <li>Получение списка карт пользователя с фильтрацией</li>
 *     <li>Изменение статуса карты</li>
 *     <li>Удаление карты</li>
 *     <li>Автоматическая проверка и обновление просроченных карт</li>
 * </ul>
 *
 * @author …
 */

@Service
@RequiredArgsConstructor
@Slf4j

public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    /**
     * Создает новую карту на основе входящего запроса.
     *
     * @param cardRequestDto данные для создания карты
     * @return {@link CardResponseDto} с информацией о созданной карте
     */
    @Override
    public CardResponseDto createCard(CardRequestDto cardRequestDto) {
        log.info("Creating new card");
        return cardMapper.cardToCardResponseDto(cardRepository.save(cardMapper.requestToCard(cardRequestDto)));
    }

    /**
     * Возвращает баланс карты по её идентификатору.
     *
     * @param cardId идентификатор карты
     * @return баланс карты
     * @throws EntityNotFoundException если карта не найдена
     * @throws IllegalStateException   если карта заблокирована или просрочена
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Карта с id %d не найдена", cardId)));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Нельзя получить баланс заблокированной карты");
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new IllegalStateException("Нельзя получить баланс просроченной карты");
        }

        return card.getBalance();
    }

    /**
     * Находит карту по её идентификатору.
     *
     * @param id идентификатор карты
     * @return {@link CardResponseDto} с информацией о карте
     * @throws CardNotFoundException если карта не найдена
     */
    @Override
    public CardResponseDto getCardById(Long id) {
        log.info("Fetching card with id: {}", id);
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found with id: " + id));
        return cardMapper.cardToCardResponseDto(card);
    }

    /**
     * Возвращает список карт пользователя с фильтрацией по номеру и статусу.
     *
     * @param userId   идентификатор пользователя
     * @param request  параметры поиска (номер карты, статус)
     * @param pageable параметры пагинации
     * @return страница с результатами поиска карт
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CardSearchResponseDto> getUserCards(Long userId, CardSearchRequestDto request, Pageable pageable) {
        Specification<Card> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("owner").get("id"), userId));

        if (request.searchTerm() != null && !request.searchTerm().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("cardNumber"), "%" + request.searchTerm() + "%"));
        }

        if (request.status() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), request.status()));
        }

        Page<Card> cards = cardRepository.findAll(spec, pageable); // Теперь будет работать

        log.info("Запрошены карты пользователя с id {}. Найдено {} карт",
                userId, cards.getTotalElements());

        return cards.map(cardMapper::cardToCardSearchResponse);
    }

    /**
     * Изменяет статус карты.
     * <p>
     * Допустимые новые статусы: ACTIVE, BLOCKED.
     * Нельзя изменять статус просроченной карты.
     * </p>
     *
     * @param cardId  идентификатор карты
     * @param request объект с новым статусом
     * @return {@link CardResponseDto} с обновленной информацией о карте
     * @throws EntityNotFoundException если карта не найдена
     * @throws IllegalArgumentException если передан недопустимый статус
     * @throws IllegalStateException    если карта уже имеет указанный статус или является просроченной
     */
    @Override
    @Transactional
    public CardResponseDto changeCardStatus(Long cardId, StatusChangeRequestDto request) {
        CardStatus newStatus;
        try {
            newStatus = CardStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимый статус карты. Допустимые значения: ACTIVE, BLOCKED, EXPIRED");
        }

        if (newStatus != CardStatus.ACTIVE && newStatus != CardStatus.BLOCKED) {
            throw new IllegalArgumentException("Можно изменить статус только на ACTIVE или BLOCKED");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Карта с id %d не найдена", cardId)));
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new IllegalStateException("Нельзя изменить статус просроченной карты");
        }

        if (card.getStatus() == newStatus) {
            throw new IllegalStateException(
                    String.format("Карта уже имеет статус: %s", newStatus));
        }
        card.setStatus(newStatus);
        Card updatedCard = cardRepository.save(card);

        log.info("Изменен статус карты. ID карты: {}, Новый статус: {}", cardId, newStatus);

        return cardMapper.cardToCardResponseDto(updatedCard);
    }

    /**
     * Удаляет карту по идентификатору.
     *
     * @param id идентификатор карты
     * @throws CardNotFoundException если карта не найдена
     */
    @Override
    public void deleteById(Long id) {
        log.info("Deleting card with id: {}", id);
        if (cardRepository.existsById(id)) {
            cardRepository.deleteById(id);
        } else throw new CardNotFoundException("Card not found with id: " + id);
    }

    /**
     * Плановое задание, выполняемое ежедневно в полночь.
     * Проверяет все активные карты и помечает их как {@link CardStatus#EXPIRED},
     * если срок действия истек. Баланс просроченных карт устанавливается в 0.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void updateExpiredCardStatuses() {
        LocalDate today = LocalDate.now();
        List<Card> activeCards = cardRepository.findByStatus(CardStatus.ACTIVE);

        if (activeCards.isEmpty()) {
            log.info("No active cards found to check.");
            return;
        }

        List<Card> expiredCards = activeCards.stream()
                .filter(card -> card.getExpirationDate().isBefore(today))
                .peek(card -> {
                    card.setStatus(CardStatus.EXPIRED);
                    card.setBalance(BigDecimal.ZERO);
                    log.warn("Card ID={} expired on {} — marked as EXPIRED and balance set to 0.",
                            card.getId(), card.getExpirationDate());
                })
                .toList();

        if (expiredCards.isEmpty()) {
            log.info("No cards expired today.");
            return;
        }

        cardRepository.saveAll(expiredCards);
        log.info("Updated {} expired card(s).", expiredCards.size());
    }
}
