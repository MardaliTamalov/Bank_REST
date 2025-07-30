package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CardSearchResponseDto;
import com.example.bankcards.entity.Card;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-29T22:50:10+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class CardMapperImpl implements CardMapper {

    @Override
    public Card requestToCard(CardRequestDto cardRequestDto) {
        if ( cardRequestDto == null ) {
            return null;
        }

        Card card = new Card();

        card.setCardNumber( maskCardNumber( cardRequestDto.cardNumber() ) );
        card.setExpirationDate( cardRequestDto.expirationDate() );
        card.setStatus( cardRequestDto.status() );
        card.setBalance( cardRequestDto.balance() );

        return card;
    }

    @Override
    public CardResponseDto cardToCardResponseDto(Card card) {
        if ( card == null ) {
            return null;
        }

        Card card1 = null;

        card1 = card;

        CardResponseDto cardResponseDto = new CardResponseDto( card1 );

        return cardResponseDto;
    }

    @Override
    public void updateCardFromDto(CardRequestDto dto, Card entity) {
        if ( dto == null ) {
            return;
        }

        entity.setCardNumber( maskCardNumber( dto.cardNumber() ) );
        entity.setExpirationDate( dto.expirationDate() );
        entity.setStatus( dto.status() );
        entity.setBalance( dto.balance() );
    }

    @Override
    public CardSearchResponseDto cardToCardSearchResponse(Card card) {
        if ( card == null ) {
            return null;
        }

        Long id = null;
        String status = null;
        BigDecimal balance = null;
        LocalDate expirationDate = null;

        id = card.getId();
        if ( card.getStatus() != null ) {
            status = card.getStatus().name();
        }
        balance = card.getBalance();
        expirationDate = card.getExpirationDate();

        String maskedCardNumber = maskCardNumber(card.getCardNumber());

        CardSearchResponseDto cardSearchResponseDto = new CardSearchResponseDto( id, maskedCardNumber, status, balance, expirationDate );

        return cardSearchResponseDto;
    }
}
