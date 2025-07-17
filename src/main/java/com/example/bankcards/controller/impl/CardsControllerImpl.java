package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.CardsController;
import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor

public class CardsControllerImpl implements CardsController {
    private final CardService cardService;

    @Override
    @PostMapping()
    public ResponseEntity<CardResponseDto> addCard(@Valid @RequestBody CardRequestDto card) {
        return ok(cardService.createCard(card));
    }

    @Override
    @GetMapping("/{id}")
      public ResponseEntity<CardResponseDto> getCard(@PathVariable Long id) {
        return ok(cardService.getCardById(id));
    }

    @Override
    @PutMapping()
       public ResponseEntity<CardResponseDto> updateCard(@Valid @RequestBody CardRequestDto card) {
        return ok(cardService.updateCard(card));
    }

    @Override
    @DeleteMapping("/{id}")
     public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteById(id);
        return ok().build(); //ResponseEntity.noContent().build();
    }
}
