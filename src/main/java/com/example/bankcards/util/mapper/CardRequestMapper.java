package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.response.CardRequestDto;
import com.example.bankcards.entity.CardRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardRequestMapper {
    CardRequestDto toDto(CardRequest cardRequest);
}
