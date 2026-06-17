package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ChangeBalanceRequest (
        @Schema(
                description = "Тип операции с балансом",
                example = "ADD",
                allowableValues = {"ADD", "SUBTRACT"}
        )
        @NotBlank(message = "Тип операции обязателен")
        @Pattern(
                regexp = "ADD|SUBTRACT",
                message = "Допустимые операции: ADD, SUBTRACT"
        )
        String operation,

        @Schema(
                description = "Сумма операции",
                example = "1500.50",
                minimum = "0.01",
                maximum = "10000000"
        )
        @NotNull(message = "Сумма обязательна")
        @DecimalMin(
                value = "0.01",
                message = "Сумма должна быть больше нуля"
        )
        @DecimalMax(
                value = "10000000",
                message = "Сумма не должна превышать 10000000"
        )
        @Digits(
                integer = 8,
                fraction = 2,
                message = "Сумма должна содержать не более 8 цифр до запятой и 2 после"
        )
        BigDecimal amount
){
}
