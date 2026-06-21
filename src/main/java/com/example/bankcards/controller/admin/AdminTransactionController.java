package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.TransactionDto;
import com.example.bankcards.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * REST-контроллер для административного просмотра транзакций пользователей.
 * Предоставляет администратору доступ к списку транзакций конкретного пользователя
 * и к информации о транзакции по её идентификатору.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Tag(name = "Администрирование транзакций",
     description = "Получение информации о транзакциях пользователей"
)
public class AdminTransactionController {
    private final TransactionService transactionService;
    /**
     * Возвращает страницу транзакций указанного пользователя.
     *
     * Поддерживает пагинацию и сортировку. По умолчанию транзакции сортируются
     * по дате создания в порядке возрастания.
     *
     * @param userId идентификатор пользователя, транзакции которого нужно получить
     * @param pageable параметры пагинации и сортировки
     * @return страница с транзакциями пользователя
     */
    @Operation(
            summary = "Получить транзакции пользователя",
            description = "Возвращает страницу транзакций указанного пользователя с поддержкой пагинации и сортировки"
    )
    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<PageResponse<TransactionDto>>findAllUserTransactions(
            @Parameter(description = "Идентификатор пользователя", example = "3")
            @PathVariable Long userId,
            @ParameterObject
            @PageableDefault(size = 5, sort = "creationTime", direction = Sort.Direction.ASC)Pageable pageable){
        log.debug("Получение транзакций пользователя с ID {}",userId);

        return ResponseEntity.ok(transactionService.findAllByUserId(pageable,userId));
    }
    /**
     * Возвращает транзакцию по её идентификатору.
     *
     * @param id идентификатор транзакции
     * @return данные найденной транзакции
     */
    @Operation(summary = "Получить транзакцию по идентификатору",
            description = "Возвращает подробную информацию о транзакции по её идентификатору")
    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionDto> findTransaction(
            @Parameter(description = "Идентификатор транзакции", example = "15")
            @PathVariable Long id){
        log.debug("Получение транзакций с ID {}",id);

        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

}