package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.TransactionsController;
import com.example.bankcards.dto.TransactionRequestDto;
import com.example.bankcards.dto.TransactionResponseDto;
import com.example.bankcards.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor

public class TransactionsControllerImpl implements TransactionsController {
    private final TransactionService transactionService;

    @PostMapping()
    public ResponseEntity<TransactionResponseDto> addTransfer(@RequestBody TransactionRequestDto transactionRequestDto) {
        return ok(transactionService.createTransfer(transactionRequestDto));
    }

   @GetMapping()
    public ResponseEntity<List<TransactionResponseDto>> getCardTransactions(
            @PathVariable String cardNumber
          //  @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ok(transactionService.getTransactionsByCardId(cardNumber));
    }



}
