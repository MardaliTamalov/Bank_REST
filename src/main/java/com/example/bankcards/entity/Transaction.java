package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Карта, с которой отправили
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    // Карта, на которую перевели
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_card_id", nullable = false)
    @JsonIgnore
    private Card toCard;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", fromCard=" + (fromCard != null ? fromCard.getCardNumber() : null) +
                ", toCard=" + (toCard != null ? toCard.getCardNumber() : null) +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
