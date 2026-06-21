package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
/**
 * Точка входа в приложение Bank REST API.
 *
 * Запускает Spring Boot приложение, включает:
 * - JPA Auditing для автоматического заполнения audit-полей (@CreatedDate и т.д.)
 * - Scheduling для фоновых задач (например, просрочка карт)
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class BankRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankRestApplication.class, args);
    }

}
