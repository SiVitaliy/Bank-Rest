package com.example.bankcards.specification;

import com.example.bankcards.dto.filter.CardRequestFilter;
import com.example.bankcards.entity.CardRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CardRequestSpecificationBuilder {


    public Specification<CardRequest> buildSpec(CardRequestFilter filter){
        return Specification
                .where(statusEquals(filter.status()))
                .and(typeEquals(filter.requestType()));
    }
    private Specification<CardRequest> statusEquals(String status){
        return (root, query, criteriaBuilder) -> {
            if (status==null || status.isBlank()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), CardRequest.RequestStatus.valueOf(status));
        };
    }
    private Specification<CardRequest> typeEquals(String type){
        return (root, query, criteriaBuilder) -> {
            if (type==null || type.isBlank()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("requestType"), CardRequest.RequestType.valueOf(type));
        };
    }

}
