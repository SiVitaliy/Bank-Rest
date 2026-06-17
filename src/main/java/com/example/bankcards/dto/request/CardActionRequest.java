package com.example.bankcards.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
@Schema(description = "Запрос на создание заявки по карте")

public record CardActionRequest(
        @Schema(description = "Тип запроса(ISSUE, BLOCK, ACTIVATE, DELETE)", example = "ISSUE",
                allowableValues = {"ISSUE", "BLOCK", "ACTIVATE", "DELETE"})
        @NotBlank(message = "Тип запроса обязателен")
        @Pattern(regexp = "ISSUE|BLOCK|ACTIVATE|DELETE",
                message = "Допустимые типы: ISSUE, BLOCK, ACTIVATE, DELETE")
        String requestType,

        @Schema(description = "ID карты (обязателен для BLOCK, ACTIVATE, DELETE; для ISSUE можно не передавать)", example = "1")
        Long cardId
) {


}
