package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.TransactionsController;
import com.example.bankcards.dto.TransactionRequestDto;
import com.example.bankcards.dto.TransactionResponseDto;
import com.example.bankcards.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor

public class TransactionsControllerImpl implements TransactionsController {
    private final TransactionService transactionService;

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TransactionResponseDto> addTransfer(@Valid @RequestBody TransactionRequestDto transactionRequestDto) {
        return ok(transactionService.createTransfer(transactionRequestDto));
    }

   @GetMapping("/{cardNumber}")
   @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<TransactionResponseDto>> getCardTransactions(
            @PathVariable String cardNumber
    ) {
        return ok(transactionService.getTransactionsByCardId(cardNumber));
    }
}
