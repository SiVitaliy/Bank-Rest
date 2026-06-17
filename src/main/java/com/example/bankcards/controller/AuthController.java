package com.example.bankcards.controller;

import com.example.bankcards.dto.response.JwtResponseDto;
import com.example.bankcards.dto.request.AuthUserRequest;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.util.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация",
        description = "Регистрация пользователей и получение JWT-токена")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Зарегистрировать пользователя",
            description = "Создаёт нового пользователя и возвращает JWT-токен")
    @PostMapping("/register")
    public ResponseEntity<JwtResponseDto> registration(
            @Valid @RequestBody AuthUserRequest registerUserRequest
    ) throws UsernameAlreadyExistsException {
        log.debug("Регистрация пользователя");
        authService.register(registerUserRequest);
        String token = jwtUtil.generateToken(registerUserRequest.username());
        return ResponseEntity.ok(new JwtResponseDto(token));
    }

    @Operation(summary = "Выполнить вход",
            description = "Проверяет имя пользователя и пароль, затем возвращает JWT-токен")
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(
            @Valid @RequestBody AuthUserRequest loginUserRequest
    ) {
        log.debug("Логин пользователя");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginUserRequest.username(),
                            loginUserRequest.password()));
            String token = jwtUtil.generateToken(authentication.getName());
            return ResponseEntity.ok(new JwtResponseDto(token));
        } catch (BadCredentialsException e) {
             throw new BadCredentialsException("Invalid username or password");
        }

    }
}