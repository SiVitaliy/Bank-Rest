package com.example.bankcards.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
@Schema(description = "Запрос на обработку заявки администратором")

public record ProcessCardRequest(
        @Schema(description = "Решение по заявке: APPROVE или REJECT", example = "APPROVE",
                allowableValues = {"APPROVE","REJECT"})
        @NotBlank(message = "Действие обязательно")
        @Pattern(regexp = "APPROVE|REJECT", message = "Допустимые значения: APPROVE, REJECT")
        String action
) {}
