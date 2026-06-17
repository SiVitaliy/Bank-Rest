package com.example.bankcards.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.YearMonth;

@Schema(description = "Запрос на создание новой карты")
public record CreateCardRequest(
        @Schema(description = "Номер карты (16 цифр)", example = "4532015112830366")
        @NotBlank(message = "Номер карты обязателен")
        @Pattern(regexp = "\\d{16}", message = "Номер карты должен содержать ровно 16 цифр")
        String cardNumber,

        @Schema(
                description = "Срок действия карты в формате MM/yy",
                example = "12/27",
                type = "string",
                pattern = "^(0[1-9]|1[0-2])/\\d{2}$"
        )
        @Future(message = "Срок действия должен быть в будущем")
        @NotNull(message = "Срок действия обязателен")
        @JsonFormat(pattern = "MM/yy")
        YearMonth expirationDate
) {
}
