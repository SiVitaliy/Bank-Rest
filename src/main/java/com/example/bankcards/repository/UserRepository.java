package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * Репозиторий для работы с пользователями.
 * Предоставляет стандартные CRUD-операции, поиск пользователя по имени,
 * проверку существования имени пользователя и постраничный поиск пользователей.
 */
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    /**
     * Возвращает страницу пользователей, имя которых содержит переданную строку поиска.
     * Поиск выполняется без учёта регистра.
     * @param search строка поиска по имени пользователя
     * @param pageable параметры пагинации и сортировки
     * @return страница найденных пользователей
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> findAllWithSearch(String search,Pageable pageable);
    /**
     * Ищет пользователя по имени.
     * Используется для аутентификации и получения пользователя по username.
     * @param username имя пользователя
     * @return найденный пользователь или пустой {@link Optional}, если пользователь не найден
     */
    Optional<User> findByUsername(String username);
    /**
     * Проверяет существование пользователя с указанным именем.
     * Используется при регистрации для предотвращения дублирования username.
     * @param username имя пользователя
     * @return {@code true}, если пользователь с таким именем уже существует
     */
    boolean existsByUsername(String username);
}
