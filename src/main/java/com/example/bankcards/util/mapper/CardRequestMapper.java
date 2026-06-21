package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.response.CardRequestDto;
import com.example.bankcards.entity.CardRequest;
import org.mapstruct.Mapper;
/**
 * Mapper для преобразования сущности CardRequest в DTO.
 * Используется для возврата данных заявок на операции с картами
 * через REST API.
 */
@Mapper(componentModel = "spring", uses = CardMapper.class)
public interface CardRequestMapper {
    /**
     * Преобразует сущность CardRequest в DTO.
     *
     * @param cardRequest сущность заявки
     * @return DTO заявки
     */
    CardRequestDto toDto(CardRequest cardRequest);
}
