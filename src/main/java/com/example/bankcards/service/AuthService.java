package com.example.bankcards.service;

import com.example.bankcards.dto.request.AuthUserRequest;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис регистрации пользователей.
 * Отвечает за создание новой учётной записи пользователя, проверку уникальности
 * имени пользователя и сохранение пароля в зашифрованном виде.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    /**
     * Регистрирует нового пользователя в системе.
     *
     * Перед созданием пользователя проверяет, что username ещё не занят.
     * Пароль сохраняется не в открытом виде, а после обработки через
     * {@link PasswordEncoder}. Новому пользователю назначается роль
     * {@link User.Role#USER}.
     *
     * @param registerUserRequest данные для регистрации пользователя
     * @throws UsernameAlreadyExistsException если пользователь с таким username уже существует
     */
    public void register(AuthUserRequest registerUserRequest) {
        log.info("Попытка регистрации пользователя: {}", registerUserRequest.username());

        if (userRepository.existsByUsername(registerUserRequest.username())) {
            log.warn("Регистрация отклонена — username уже существует: {}", registerUserRequest.username());
            throw new UsernameAlreadyExistsException();
        }

        User user = new User();
        user.setUsername(registerUserRequest.username());
        user.setPassword(passwordEncoder.encode(registerUserRequest.password()));
        user.setRole(User.Role.USER);

        userRepository.save(user);

        log.info("Пользователь успешно зарегистрирован: {}", user.getUsername());
    }


}
