package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository <Card, Long>,
        JpaSpecificationExecutor<Card> {
    Optional<Card> findByCardNumber(String cardNumber);
    List<Card> findByStatus(CardStatus status);
}
