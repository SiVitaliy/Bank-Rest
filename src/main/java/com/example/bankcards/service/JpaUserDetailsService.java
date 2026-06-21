package com.example.bankcards.service;

import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
/**
 * Сервис загрузки пользователей для Spring Security.
 *
 * Используется механизмом аутентификации для поиска пользователя
 * по имени пользователя и получения его данных безопасности:
 * пароля, роли, authorities и статуса активности.
 */
@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    /**
     * Загружает пользователя по имени пользователя.
     *
     * Метод вызывается Spring Security во время аутентификации,
     * а также может использоваться при JWT-аутентификации для получения
     * актуальных данных пользователя из базы.
     *
     * @param username имя пользователя
     * @return данные пользователя для Spring Security
     * @throws UsernameNotFoundException если пользователь с указанным именем не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}