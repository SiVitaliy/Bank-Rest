package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
/**
 * Entity пользователя системы.
 *
 * Хранит данные учётной записи пользователя, его роль, статус активности,
 * дату создания и связанные банковские карты. Используется Spring Security
 * как реализация {@link UserDetails} для аутентификации и авторизации.
 */
@Entity
@Table(name="users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
    public enum Role {
        USER,ADMIN
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name="username", nullable = false,unique = true)
    private String username;
    @Column(name="password", nullable = false)
    private String password;
    @Column(name = "role",nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name="creation_time",nullable = false)
    @CreatedDate
    private LocalDateTime creationTime;
    @OneToMany(mappedBy = "owner",orphanRemoval = true)
    private List<Card> cards;
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }


    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
