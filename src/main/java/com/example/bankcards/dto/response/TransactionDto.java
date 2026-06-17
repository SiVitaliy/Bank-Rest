package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Schema(description = "Данные о проведённой транзакции")
public record TransactionDto(
        @Schema(description = "ID транзакции", example = "42")
        Long id,

        @Schema(description = "Карта отправитель")
        CardDto fromCard,

        @Schema(description = "Карта получатель")
        CardDto toCard,

        @Schema(description = "Время выполнения транзакции", example = "2026-06-15T14:30:00")
        LocalDateTime creationTime,

        @Schema(description = "Сумма перевода", example = "150.00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "#0.00")
        BigDecimal amount
) {}