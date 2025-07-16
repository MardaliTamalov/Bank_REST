package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")

public interface CardMapper {

    Card requestToCard(CardRequestDto cardRequestDto);

    CardResponseDto cardToCardResponseDto(Card card);

    @Mapping(target = "id", ignore = true)
    void updateCardFromDto(CardRequestDto dto, @MappingTarget Card entity);
}
