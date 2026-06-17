package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
@Schema(description = "Заявка на действие с картой (блокировка, активация, выпуск)")

public record CardRequestDto(
        @Schema(description = "ID заявки", example = "10")
        Long id,
        @Schema(description = "Пользователь, создавший заявку")
        UserDto requester,
        @Schema(description = "Время создания заявки", example = "2026-06-10T10:30:00")
        LocalDateTime creationTime,
        @Schema(description = "Статус заявки", example = "PENDING")
        String status,
        @Schema(description = "Тип запроса", example = "BLOCK")
        String requestType,
        @Schema(description = "Карта, к которой относится заявка (может быть null для ISSUE)")
        CardDto card,
        @Schema(description = "Администратор, обработавший заявку (null, если ещё не обработана)")
        UserDto processedBy,

        @Schema(description = "Дата последнего обновления", example = "2026-06-10T12:45:00")
        LocalDateTime updatedAt
) {}