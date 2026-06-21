package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.JpaUserDetailsService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/**
 * Фильтр аутентификации пользователя по JWT-токену.
 * Выполняется один раз на каждый HTTP-запрос. Извлекает JWT из заголовка
 * Authorization, проверяет его валидность, загружает пользователя из базы
 * и устанавливает объект аутентификации в {@link SecurityContextHolder}.
 */
@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JpaUserDetailsService jpaUserDetailsService;
    /**
     * Обрабатывает входящий HTTP-запрос и выполняет JWT-аутентификацию.
     * Если в заголовке Authorization передан Bearer-токен и он валиден,
     * фильтр извлекает username из токена, загружает пользователя и помещает
     * аутентификацию в security context.
     * @param request входящий HTTP-запрос
     * @param response HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException если произошла ошибка обработки servlet-запроса
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                UserDetails user = jpaUserDetailsService.loadUserByUsername(username);
                if (user.isEnabled()) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Установлен JWT для {} с ролью {}", username, user.getAuthorities());
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
