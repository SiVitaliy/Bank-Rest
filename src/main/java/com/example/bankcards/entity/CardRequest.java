package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "card_request")
@EntityListeners(AuditingEntityListener.class)
public class CardRequest {
    public enum RequestType{ISSUE, BLOCK, ACTIVATE,DELETE}
    public enum RequestStatus{PENDING, APPROVED, REJECTED}

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creation_time", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime creationTime;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne()
    @JoinColumn(name = "requester_id",referencedColumnName = "id", nullable = false)
    private User requester;

    @ManyToOne()
    @JoinColumn(name = "card_id",referencedColumnName = "id")
    private Card card;

    @ManyToOne()
    @JoinColumn(name = "processed_by",referencedColumnName = "id")
    private User processedBy;



}
