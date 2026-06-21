package com.example.bankcards.util.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
/**
 * Утилита для работы с JWT токенами.
 * Отвечает за генерацию, валидацию и извлечение данных из JWT.
 * Используется в процессе аутентификации и авторизации пользователей.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;
    /**
     * Формирует ключ подписи на основе Base64 секретного значения.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    /**
     * Генерирует JWT токен для пользователя.
     *
     * @param username username (в данном случае используется как subject)
     * @return JWT токен
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Извлекает username (subject) из JWT токена.
     *
     * @param token JWT токен
     * @return username пользователя
     */

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }
    /**
     * Проверяет корректность JWT токена.
     *
     * @param token JWT токен
     * @return true если токен валиден, иначе false
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Парсит JWT и возвращает claims.
     *
     * @param token JWT токен
     * @return payload токена
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}