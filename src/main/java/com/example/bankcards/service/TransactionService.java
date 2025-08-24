package com.example.bankcards.service;

import com.example.bankcards.dto.TransactionRequestDto;
import com.example.bankcards.dto.TransactionResponseDto;
import com.example.bankcards.entity.Transaction;

import java.util.List;

public interface TransactionService {

    TransactionResponseDto createTransfer(TransactionRequestDto transactionRequestDto);

    List<TransactionResponseDto> getTransactionsByCardId(String cardNumber);

}
