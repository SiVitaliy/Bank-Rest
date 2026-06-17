package com.example.bankcards.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с информацией об ошибке")
public record ErrorResponseDto(

        @Schema(description = "Машиночитаемый код ошибки", example = "CARD_NOT_FOUND")
        String code,

        @Schema(description = "Описание ошибки", example = "Карта с id 10 не найдена")
        String message
) {
}