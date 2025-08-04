package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.CardsController;
import com.example.bankcards.dto.*;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor

public class CardsControllerImpl implements CardsController {
    private final CardService cardService;

    @Override
    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> addCard(@Valid @RequestBody CardRequestDto card) {
        return ok(cardService.createCard(card));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CardResponseDto> getCard(@PathVariable Long id) {
        return ok(cardService.getCardById(id));
    }

    @Override
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<CardSearchResponseDto>> getUserCards(
            @PathVariable Long userId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        CardSearchRequestDto searchRequest = new CardSearchRequestDto(search, status);
        Page<CardSearchResponseDto> cards = cardService.getUserCards(userId, searchRequest, pageable);
        return ResponseEntity.ok(cards);
    }

    @Override
    @GetMapping("/{cardId}/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<BigDecimal> getCardBalance(@PathVariable Long cardId) {
        BigDecimal balance = cardService.getCardBalance(cardId);
        return ResponseEntity.ok(balance);
    }

    @Override
    @PatchMapping("/{cardId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> changeCardStatus(
            @PathVariable Long cardId,
            @RequestBody StatusChangeRequestDto request) {
        CardResponseDto updatedCard = cardService.changeCardStatus(cardId, request);
        return ResponseEntity.ok(updatedCard);
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteById(id);
        return ok().build(); //ResponseEntity.noContent().build();
    }
}

