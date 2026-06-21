package com.example.bankcards.specification;

import com.example.bankcards.dto.filter.CardFilter;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.EncryptionService;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
/**
 * Построитель спецификаций для фильтрации банковских карт.
 *
 * Поддерживает фильтрацию:
 * - по владельцу (для пользователя)
 * - по статусу карты
 * - по дате создания
 * - по поиску (last4 или полный номер карты)
 */
@Component
@AllArgsConstructor
public class CardSpecificationBuilder {
    private EncryptionService encryptionService;
    /**
     * Спецификация для администратора (без ограничения по владельцу).
     */
    public Specification<Card> buildSpecForAdmin(CardFilter filter){
        return Specification
                .where(statusEquals(filter.status()))
                .and(searchContains(filter.search()))
                .and(createdFrom(filter.createdFrom()))
                .and(createdTo(filter.createdTo()));
    }
    /**
     * Спецификация для пользователя (с ограничением по ownerId).
     */
    public Specification<Card> buildSpecForUser(CardFilter filter, Long ownerId) {
        return Specification
                .where(ownerIdEquals(ownerId))
                .and(createdFrom(filter.createdFrom()))
                .and(createdTo(filter.createdTo()))
                .and(statusEquals(filter.status()))
                .and(searchContains(filter.search()));
    }

    /**
     * Фильтр по владельцу карты.
     */
    private Specification<Card> ownerIdEquals(Long ownerId) {
        return (root, query, cb) ->
                cb.equal(root.get("owner").get("id"), ownerId);
    }
    /**
     * Фильтр по статусу карты.
     */
    private Specification<Card> statusEquals(String status){
        return ((root, query, criteriaBuilder) ->{
            if(status==null || status.isBlank()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), Card.CardStatus.valueOf(status));
        }
        );
    }
    /**
     * Поиск по карте:
     * - 1–4 цифры → поиск по lastFour
     * - 16 цифр → поиск по encrypted cardNumber
     */
    private Specification<Card> searchContains(String search){
        return (root, query, criteriaBuilder) -> {
            if(search==null || search.isBlank()){
                return criteriaBuilder.conjunction();
            }
            String normalized = search.replaceAll("\\D", "");
            if (normalized.length()<=4 && !normalized.isEmpty()) {
                return criteriaBuilder.like(root.get("lastFour"), "%"+normalized+"%");
            }
            if (normalized.length()==16){
                return criteriaBuilder.equal(root.get("cardNumber"),encryptionService.encrypt(normalized));
            }
            return criteriaBuilder.disjunction();
        };
    }
    /**
     * Фильтр по дате создания (от).
     */
    private Specification<Card> createdFrom(LocalDateTime createdFrom){
        return (root, query, criteriaBuilder) -> {
            if (createdFrom==null){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("creationTime"),createdFrom);
        };
    }

    /**
     * Фильтр по дате создания (до).
     */
    private Specification<Card> createdTo(LocalDateTime createdTo){
        return (root, query, criteriaBuilder) -> {
            if (createdTo==null){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get("creationTime"),createdTo);
        };
    }

}
