package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * Репозиторий для работы с транзакциями.
 * Предоставляет стандартные CRUD-операции, а также методы для получения
 * транзакций конкретного пользователя и проверки принадлежности транзакции
 * пользователю.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    /**
     * Возвращает страницу транзакций указанного пользователя.
     * @param user владелец транзакций
     * @param pageable параметры пагинации и сортировки
     * @return страница транзакций пользователя
     */

    Page<Transaction> findAllByOwner(User user, Pageable pageable);
    /**
     * Возвращает страницу транзакций пользователя по его идентификатору.
     * @param user идентификатор пользователя
     * @param pageable параметры пагинации и сортировки
     * @return страница транзакций пользователя
     */
    Page<Transaction> findAllByOwnerId(Long user, Pageable pageable);
    /**
     * Ищет транзакцию по идентификатору с проверкой её принадлежности пользователю.
     * Используется для того, чтобы пользователь мог получить только свою транзакцию.
     * @param id идентификатор транзакции
     * @param user пользователь, которому должна принадлежать транзакция
     * @return найденная транзакция или пустой {@link Optional}, если транзакция не найдена
     */
    @Query("""
            select t
            from  Transaction t
            where t.owner=:user and t.id=:id
            """)
    Optional<Transaction> findByIdForUser(Long id, User user);
}
