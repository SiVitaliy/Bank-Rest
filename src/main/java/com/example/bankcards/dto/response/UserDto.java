package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
@Schema(description = "Публичная информация о пользователе")
public record UserDto(
        @Schema(description = "Уникальный идентификатор пользователя", example = "1")
        Long id,

        @Schema(description = "Имя пользователя (логин)", example = "alexey")
        String username,

        @Schema(description = "Роль пользователя", example = "USER",
                allowableValues = {"USER", "ADMIN"})
        String role,

        @Schema(description = "Дата и время создания учётной записи", example = "2026-06-15T10:00:00")
        LocalDateTime creationTime,
        @Schema(description = "Разблокирован ли пользователь", example = "true")
        boolean enabled

) {}