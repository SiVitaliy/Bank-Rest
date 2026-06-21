package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/**
 * Репозиторий для работы с банковскими картами.
 * Предоставляет стандартные CRUD-операции, поиск по спецификациям,
 * выборку карт пользователя, проверку существования карты по номеру
 * и массовое обновление статуса истёкших карт.
 */
@Repository

public interface CardRepository extends JpaRepository<Card, Long>,
                                        JpaSpecificationExecutor<Card> {

    /**
     * Возвращает страницу карт по переданной спецификации.
     * Для предотвращения N+1-запросов вместе с картами загружаются
     * связанные сущности владельца карты и администратора, создавшего карту.
     * @param spec спецификация для фильтрации карт
     * @param pageable параметры пагинации и сортировки
     * @return страница найденных карт
     */
    @EntityGraph(attributePaths = {"owner", "createdByAdmin"})
    Page<Card> findAll(Specification<Card> spec, Pageable pageable);
    /**
     * Ищет карту по идентификатору карты и идентификатору владельца.
     * Используется для проверки, что пользователь обращается только
     * к собственной карте.
     * @param cardId идентификатор карты
     * @param userId идентификатор владельца карты
     * @return найденная карта или пустой {@link Optional}, если карта не найдена
     */

    Optional<Card> findByIdAndOwnerId(Long cardId, Long userId);
    /**
     * Массово переводит просроченные карты в статус {@code EXPIRED}.
     * Обновляет только те карты, у которых дата окончания срока действия
     * меньше текущей даты и текущий статус ещё не равен статусу истёкшей карты.
     * @param currentDate текущая дата
     * @param expiredStatus статус истёкшей карты
     * @return количество обновлённых записей
     */
    @Modifying
    @Query("""
        update Card c
        set c.status = :expiredStatus
         where c.expirationDate < :currentDate
        and c.status <> :expiredStatus
        """)
    int markExpiredCards(@Param("currentDate") LocalDate currentDate,
                         @Param("expiredStatus") Card.CardStatus expiredStatus);
    /**
     * Проверяет существование карты по зашифрованному номеру.
     * @param encryptedNumber зашифрованный номер карты
     * @return {@code true}, если карта с таким зашифрованным номером существует
     */
    boolean existsByCardNumber(String encryptedNumber);
}
