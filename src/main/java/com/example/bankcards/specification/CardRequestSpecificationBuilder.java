package com.example.bankcards.specification;

import com.example.bankcards.dto.filter.CardRequestFilter;
import com.example.bankcards.entity.CardRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
/**
 * Компонент для построения спецификаций фильтрации заявок на операции с картами.
 *
 * Формирует {@link Specification} на основе параметров фильтра:
 * статуса заявки и типа операции.
 */
@Component
public class CardRequestSpecificationBuilder {

    /**
     * Создаёт спецификацию для поиска заявок на операции с картами.
     *
     * Если отдельные параметры фильтра не переданы, по ним не применяется
     * дополнительное условие фильтрации.
     *
     * @param filter параметры фильтрации заявок
     * @return спецификация для поиска заявок
     */
    public Specification<CardRequest> buildSpec(CardRequestFilter filter){
        return Specification
                .where(statusEquals(filter.status()))
                .and(typeEquals(filter.requestType()));
    }
    /**
     * Создаёт условие фильтрации по статусу заявки.
     *
     * Если статус не передан или передана пустая строка, возвращается
     * нейтральное условие, не влияющее на результат запроса.
     *
     * @param status строковое представление статуса заявки
     * @return спецификация фильтрации по статусу
     */
    private Specification<CardRequest> statusEquals(String status){
        return (root, query, criteriaBuilder) -> {
            if (status==null || status.isBlank()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), CardRequest.RequestStatus.valueOf(status));
        };
    }
    /**
     * Создаёт условие фильтрации по типу заявки.
     *
     * Если тип не передан или передана пустая строка, возвращается
     * нейтральное условие, не влияющее на результат запроса.
     *
     * @param type строковое представление типа заявки
     * @return спецификация фильтрации по типу заявки
     */
    private Specification<CardRequest> typeEquals(String type){
        return (root, query, criteriaBuilder) -> {
            if (type==null || type.isBlank()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("requestType"), CardRequest.RequestType.valueOf(type));
        };
    }

}
