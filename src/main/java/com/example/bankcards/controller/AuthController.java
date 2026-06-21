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
/**
 * REST-контроллер для регистрации пользователей и аутентификации.
 *
 * Предоставляет возможность для создания новой учетной записи и получения JWT-токена
 * по имени пользователя и паролю.
 */
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
    /**
     * Регистрирует нового пользователя и возвращает JWT-токен.
     *
     * После успешного создания учетной записи токен генерируется по имени
     * зарегистрированного пользователя.
     *
     * @param registerUserRequest данные для регистрации пользователя
     * @return JWT-токен зарегистрированного пользователя
     * @throws UsernameAlreadyExistsException если пользователь с таким именем уже существует
     */
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
    /**
     * Выполняет аутентификацию пользователя и возвращает JWT-токен.
     *
     * Проверяет переданные имя пользователя и пароль через {@link AuthenticationManager}.
     * При успешной аутентификации генерирует JWT-токен для дальнейшего доступа
     * к защищенным возможностям API.
     *
     * @param loginUserRequest данные для входа пользователя
     * @return JWT-токен аутентифицированного пользователя
     * @throws BadCredentialsException если имя пользователя или пароль неверны
     */
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