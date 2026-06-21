package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.filter.CardRequestFilter;
import com.example.bankcards.dto.response.CardRequestDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.request.ProcessCardRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
/**
 * REST-контроллер для обработки заявок на операции с картами для администратора.
 * Предоставляет администратору возможность просматривать заявки пользователей,
 * фильтровать их по типу и статусу, а также одобрять или отклонять заявки.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/card-requests")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Админ: заявки", description = "Обработка заявок на блокировку, активацию и выпуск карт")
public class AdminCardRequestController {

    private final CardRequestService cardRequestService;
    /**
     * Возвращает страницу заявок на операции с картами.
     * Поддерживает фильтрацию по типу заявки и статусу, а также пагинацию
     * и сортировку. По умолчанию заявки сортируются по дате создания
     * в порядке убывания.
     * @param filter параметры фильтрации заявок
     * @param pageable параметры пагинации и сортировки
     * @return страница с заявками на операции с картами
     */
    @Operation(summary = "Получить все заявки",
            description = "Возвращает список всех заявок с пагинацией и сортировкой по дате создания (по убыванию)")

    @GetMapping()
    public ResponseEntity<PageResponse<CardRequestDto>> findAllRequests(
            @Valid @ParameterObject CardRequestFilter filter,
            @ParameterObject
            @PageableDefault(
                    size = 5,
                    sort = "creationTime",
                    direction = Sort.Direction.DESC
            )Pageable pageable) {
        log.debug("Получение всех заявок");

        return ResponseEntity.ok(cardRequestService.findAllRequests(filter,pageable));
    }
    /**
     * Обрабатывает заявку на операцию с картой.
     * Администратор может одобрить или отклонить заявку.
     * @param id идентификатор обрабатываемой заявки
     * @param request решение администратора по заявке
     * @param user текущий аутентифицированный администратор
     * @return обновлённая заявка после обработки
     */
    @Operation(summary = "Обработать заявку",
            description = "Одобряет (APPROVE) или отклоняет (REJECT) заявку. " +
                    "При одобрении выполняет действие: блокирует, активирует или выпускает карту.")
    @PatchMapping("/{id}")
    public ResponseEntity<CardRequestDto> processRequest(
            @Parameter(description = "ID заявки", example = "5") @PathVariable Long id,
            @Parameter(description = "Решение и опциональный комментарий") @Valid @RequestBody ProcessCardRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        log.debug("Обработка заявки с ID {}",id);
        return ResponseEntity.ok(cardRequestService.process(id, request, user));
    }
}