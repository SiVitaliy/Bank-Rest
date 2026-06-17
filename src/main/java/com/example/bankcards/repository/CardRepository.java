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

@Repository

public interface CardRepository extends JpaRepository<Card, Long>,
                                        JpaSpecificationExecutor<Card> {

    @EntityGraph(attributePaths = {"owner", "createdByAdmin"})
    Page<Card> findAll(Specification<Card> spec, Pageable pageable);

    Optional<Card> findByIdAndOwnerId(Long cardId, Long userId);
    @Modifying
    @Query("""
        update Card c
        set c.status = :expiredStatus
         where c.expirationDate < :currentDate
        and c.status <> :expiredStatus
        """)
    int markExpiredCards(@Param("currentDate") LocalDate currentDate,
                         @Param("expiredStatus") Card.CardStatus expiredStatus);

    boolean existsByCardNumber(String encryptedNumber);
}
