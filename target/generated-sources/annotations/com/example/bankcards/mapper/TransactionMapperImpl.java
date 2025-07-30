package com.example.bankcards.mapper;

import com.example.bankcards.dto.TransactionResponseDto;
import com.example.bankcards.entity.Transaction;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-29T22:50:11+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionResponseDto toDto(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        Transaction transaction1 = null;

        transaction1 = transaction;

        TransactionResponseDto transactionResponseDto = new TransactionResponseDto( transaction1 );

        return transactionResponseDto;
    }
}
