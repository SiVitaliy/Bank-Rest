package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.response.TransactionDto;
import com.example.bankcards.entity.Transaction;
import org.mapstruct.Mapper;
import org.springframework.data.jpa.repository.JpaRepository;
@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionDto toDto(Transaction transaction);
}
