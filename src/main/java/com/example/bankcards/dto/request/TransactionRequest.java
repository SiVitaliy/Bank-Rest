package com.example.bankcards.dto.request;

import com.example.bankcards.dto.response.CardDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Schema(description = "Запрос на перевод средств между картами одного пользователя")

public record TransactionRequest(
        @Schema(description = "ID карты отправителя", example = "1")
        @NotNull(message = "ID карты-отправителя обязателен")
        Long fromCardId,
        @Schema(description = "ID карты получателя", example = "2")
        @NotNull(message = "ID карты-получателя обязателен")
        Long toCardId,
        @Schema(description = "Сумма перевода", example = "150.00")
        @NotNull(message = "Сумма обязательна")
        @Positive(message = "Сумма должна быть положительной")
        BigDecimal amount
) {
}
