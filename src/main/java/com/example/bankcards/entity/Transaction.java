package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="transaction")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
    @Id
    @Column(name = "id",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "owner_id",referencedColumnName = "id",nullable = false)
    private User owner;
    @ManyToOne
    @JoinColumn(name = "from_card",referencedColumnName = "id")
    private Card fromCard;
    @ManyToOne
    @JoinColumn(name = "to_card",referencedColumnName = "id")
    private Card toCard;
    @Column(name = "amount",nullable = false)
    private BigDecimal amount;

    @CreatedDate
    @Column(name = "creation_time", nullable = false, updatable = false)
    private LocalDateTime creationTime;

}
