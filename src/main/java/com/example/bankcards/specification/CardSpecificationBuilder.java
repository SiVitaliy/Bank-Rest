package com.example.bankcards.specification;

import com.example.bankcards.dto.filter.CardFilter;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.EncryptionService;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class CardSpecificationBuilder {
    private EncryptionService encryptionService;
    public Specification<Card> buildSpecForAdmin(CardFilter filter){
        return Specification
                .where(statusEquals(filter.status()))
                .and(searchContains(filter.search()))
                .and(createdFrom(filter.createdFrom()))
                .and(createdTo(filter.createdTo()));
    }
    public Specification<Card> buildSpecForUser(CardFilter filter, Long ownerId) {
        return Specification
                .where(ownerIdEquals(ownerId))
                .and(createdFrom(filter.createdFrom()))
                .and(createdTo(filter.createdTo()))
                .and(statusEquals(filter.status()))
                .and(searchContains(filter.search()));
    }
    private Specification<Card> ownerIdEquals(Long ownerId) {
        return (root, query, cb) ->
                cb.equal(root.get("owner").get("id"), ownerId);
    }
    private Specification<Card> statusEquals(String status){
        return ((root, query, criteriaBuilder) ->{
            if(status==null || status.isBlank()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), Card.CardStatus.valueOf(status));
        }
        );
    }
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
    private Specification<Card> createdFrom(LocalDateTime createdFrom){
        return (root, query, criteriaBuilder) -> {
            if (createdFrom==null){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("creationTime"),createdFrom);
        };
    }
    private Specification<Card> createdTo(LocalDateTime createdTo){
        return (root, query, criteriaBuilder) -> {
            if (createdTo==null){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get("creationTime"),createdTo);
        };
    }

}
