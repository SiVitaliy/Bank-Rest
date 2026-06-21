package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
/**
 * Репозиторий для работы с заявками на операции с банковскими картами.
 * Предоставляет стандартные CRUD-операции, поиск по спецификациям
 * и постраничную выборку заявок с предварительной загрузкой связанных пользователей.
 */
@Repository
public interface CardRequestRepository extends JpaRepository<CardRequest, Long>,
                                               JpaSpecificationExecutor<CardRequest> {
    /**
     * Возвращает страницу заявок по переданной спецификации.
     * Для предотвращения N+1-запросов вместе с заявками загружаются
     * пользователь, создавший заявку, и администратор, обработавший заявку.
     * @param spec спецификация для фильтрации заявок
     * @param pageable параметры пагинации и сортировки
     * @return страница найденных заявок
     */
    @EntityGraph(attributePaths = {"requester", "processedBy"})
    Page<CardRequest> findAll(Specification<CardRequest> spec, Pageable pageable);
}
