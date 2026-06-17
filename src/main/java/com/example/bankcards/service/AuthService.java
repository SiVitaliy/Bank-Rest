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

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public void register(AuthUserRequest registerUserRequest) throws UsernameAlreadyExistsException {
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
