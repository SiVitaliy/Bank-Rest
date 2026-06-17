package com.example.bankcards.dto.filter;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CardFilter(
        @Parameter(
                description = "Статус карты",
                example = "ACTIVE"
        )
        @Schema(allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"})
        @Pattern(
                regexp = "ACTIVE|BLOCKED|EXPIRED",
                message = "status должен быть одним из значений: ACTIVE, BLOCKED, EXPIRED"
        )
        String status,
        @Parameter(
                description = "Поисковая строка по карте",
                example = "1234"
        )
        @Size(max = 16, message = "search не должен быть длиннее 16 символов")
        String search,

        @Parameter(
                description = "Дата и время создания карты, начиная с которой выполнять поиск",
                example = "2026-01-01T00:00:00"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdFrom,

        @Parameter(
                description = "Дата и время создания карты, до которой выполнять поиск",
                example = "2026-02-01T00:00:00"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdTo



) {

    @AssertTrue(message = "createdTo должен быть позже createdFrom")
    public boolean isValidCreatedPeriod() {
        if (createdFrom == null || createdTo == null) {
            return true;
        }

        return createdTo.isAfter(createdFrom);
    }
}
