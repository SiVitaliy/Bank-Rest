package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.response.TransactionDto;
import com.example.bankcards.entity.Transaction;
import org.mapstruct.Mapper;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Mapper для преобразования сущности Transaction в DTO.
 *
 * Используется для формирования ответа API по транзакциям:
 * переводы между картами, история операций и административный просмотр.
 */
@Mapper(componentModel = "spring", uses = CardMapper.class)
public interface TransactionMapper {
    /**
     * Преобразует сущность Transaction в TransactionDto.
     *
     * @param transaction сущность транзакции
     * @return DTO транзакции
     */
    TransactionDto toDto(Transaction transaction);
}
