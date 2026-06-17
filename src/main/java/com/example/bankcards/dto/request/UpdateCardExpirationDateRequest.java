package com.example.bankcards.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;

public record UpdateCardExpirationDateRequest(

        @Schema(
                description = "Новый срок действия карты в формате MM/yy",
                example = "12/29",
                type = "string",
                pattern = "^(0[1-9]|1[0-2])/\\d{2}$"
        )
        @NotNull(message = "Новый срок действия обязателен")
        @Future(message = "Новый срок действия должен быть в будущем")
        @JsonFormat(pattern = "MM/yy")
        YearMonth expirationDate
) {
}