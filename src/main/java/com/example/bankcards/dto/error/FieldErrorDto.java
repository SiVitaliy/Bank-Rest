package com.example.bankcards.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ошибка валидации отдельного поля")
public record FieldErrorDto(

        @Schema(description = "Название поля с ошибкой", example = "amount")
        String field,

        @Schema(description = "Описание ошибки валидации", example = "Сумма должна быть больше нуля")
        String message
) {
}
