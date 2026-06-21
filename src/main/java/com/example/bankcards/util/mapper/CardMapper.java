package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.response.CardDto;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
/**
 * Mapper для преобразования сущности Card в DTO.
 * Используется для формирования ответа API, включая маскирование номера карты.
 */
@Mapper(componentModel = "spring")
public interface CardMapper {


    /**
     * Преобразует сущность Card в CardDto.
     *
     * Полный номер карты не возвращается наружу — вместо этого
     * формируется маска вида: **** **** **** 1234.
     *
     * @param card сущность карты
     * @return DTO карты для API
     */
    @Mapping(target = "maskedCardNumber", expression = "java(\"**** **** **** \" + card.getLastFour())")
    CardDto toDto(Card card);



}
