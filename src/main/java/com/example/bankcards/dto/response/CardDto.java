package com.example.bankcards.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Schema(description = "Информация о банковской карте")
public record CardDto (
        @Schema(description = "Уникальный идентификатор карты", example = "1")
        Long id,
        @Schema(description = "Замаскированный номер карты", example = "**** **** **** 1234")
        String maskedCardNumber,
        @Schema(description = "Владелец карты")
        UserDto owner,
        @Schema(description = "Дата и время создания карты", example = "2026-06-10T12:00:00")
        LocalDateTime creationTime,
        @Schema(description = "Срок действия (месяц/год)", example = "2028-12-31")
        LocalDate expirationDate,
        @Schema(description = "Статус карты", example = "ACTIVE")
        String status,
        @Schema(description = "Текущий баланс", example = "1500.00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "#0.00")
        BigDecimal balance
){

}