package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.response.CardDto;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "cardNumber", expression = "java(\"**** **** **** \" + card.getLastFour())")
    CardDto toDto(Card card);



}
