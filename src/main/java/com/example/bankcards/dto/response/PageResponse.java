package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Schema(description = "Ответ с пагинацией")
public record PageResponse<T>(
        @Schema(description = "Список элементов на текущей странице")
        List<T> content,

        @Schema(description = "Номер текущей страницы (0-based)", example = "0")
        int page,

        @Schema(description = "Размер страницы", example = "20")
        int size,

        @Schema(description = "Общее количество элементов", example = "125")
        long totalElements,

        @Schema(description = "Общее количество страниц", example = "7")
        int totalPages,

        @Schema(description = "Есть ли следующая страница", example = "true")
        boolean hasNext,

        @Schema(description = "Есть ли предыдущая страница", example = "false")
        boolean hasPrevious
) {
    public static <T, U> PageResponse<U> from(Page<T> page, Function<T, U> mapper) {
        Page<U> mappedPage = page.map(mapper);
        return from(mappedPage);
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}