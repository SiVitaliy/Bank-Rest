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

@Repository
public interface CardRequestRepository extends JpaRepository<CardRequest, Long>,
                                               JpaSpecificationExecutor<CardRequest> {

    @EntityGraph(attributePaths = {"requester", "processedBy"})
    Page<CardRequest> findAll(Specification<CardRequest> spec, Pageable pageable);
}
