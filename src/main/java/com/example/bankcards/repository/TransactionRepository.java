package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromCard(Card fromCard);

    List<Transaction> findByToCard(Card toCard);

    List<Transaction> findByFromCardOrToCard(Card fromCard, Card toCard);
}
