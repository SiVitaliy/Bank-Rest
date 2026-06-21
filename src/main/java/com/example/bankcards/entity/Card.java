package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * Entity банковской карты.
 * Хранит зашифрованный номер карты, последние четыре цифры для отображения,
 * владельца карты, администратора, выпустившего карту, срок действия, статус и
 * баланс.
 */
@Entity
@Table(name="cards")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Card {
    public enum CardStatus {
        ACTIVE,BLOCKED,EXPIRED

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "card_number",nullable = false,unique = true)
    private String cardNumber;

    @Column(name = "last_four",nullable = false)
    private String lastFour;

    @ManyToOne
    @JoinColumn(name = "owner_id",referencedColumnName = "id",nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "created_by_admin_id",referencedColumnName = "id",nullable = false)
    private User createdByAdmin;

    @Column(name="creation_time",nullable = false)
    @CreatedDate
    private LocalDateTime creationTime;

    @Column(name = "expiration_date",nullable = false)
    private LocalDate expirationDate;

    @Column(name = "status",nullable = false)
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(name = "balance",nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    private Long version;



}
