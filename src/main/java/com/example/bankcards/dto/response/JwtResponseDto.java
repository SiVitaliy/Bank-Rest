package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Ответ с JWT-токеном после успешной аутентификации")
public record JwtResponseDto(
        @Schema(description = "JWT токен доступа", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,

        @Schema(description = "Тип токена", example = "Bearer")
        String type
) {
    public JwtResponseDto(String token) {
        this(token, "Bearer");
    }
}
