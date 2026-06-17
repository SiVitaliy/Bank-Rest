package com.example.bankcards.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Schema(description = "Запрос на аутентификацию")
public record AuthUserRequest(
      //todo вернуть мин сайз на 5
        @Schema(description = "Имя пользователя", example = "username")
        @NotBlank(message = "Имя пользователя обязательно")
        @Size(min = 1, max = 50, message = "Имя пользователя должно быть от 5 до 50 символов")
        String username,
        @Schema(description = "Пароль", example = "password")
        @NotBlank(message = "Пароль обязателен")
        @Size(min = 1, max = 100, message = "Пароль должен быть от 5 до 100 символов")
        String password) {

}
