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

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {


    Page<Transaction> findAllByOwner(User user, Pageable pageable);
    Page<Transaction> findAllByOwnerId(Long user, Pageable pageable);

    @Query("""
            select t
            from  Transaction t
            where t.owner=:user and t.id=:id
            """)
    Optional<Transaction> findByIdForUser(Long id, User user);
}
