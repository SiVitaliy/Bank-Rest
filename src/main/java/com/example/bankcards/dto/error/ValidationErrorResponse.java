package com.example.bankcards.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Ответ со списком ошибок валидации")
public record ValidationErrorResponse(

        @Schema(description = "Машиночитаемый код ошибки", example = "VALIDATION_ERROR")
        String code,

        @Schema(description = "Общее описание ошибки", example = "Ошибка валидации входных данных")
        String message,

        @Schema(description = "Список ошибок валидации по полям")
        List<FieldErrorDto> errors
) {
}