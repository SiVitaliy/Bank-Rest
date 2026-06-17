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

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Tag(name = "Администрирование транзакций",
     description = "Получение информации о транзакциях пользователей"
)
public class AdminTransactionController {
    private final TransactionService transactionService;

    @Operation(
            summary = "Получить транзакции пользователя",
            description = "Возвращает страницу транзакций указанного пользователя с поддержкой пагинации и сортировки"
    )
    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<PageResponse<TransactionDto>>findAllUserTransaction(
            @Parameter(description = "Идентификатор пользователя", example = "3")
            @PathVariable Long userId,
            @ParameterObject
            @PageableDefault(size = 5, sort = "creationTime", direction = Sort.Direction.ASC)Pageable pageable){
        log.debug("Получение транзакций пользователя с ID {}",userId);

        return ResponseEntity.ok(transactionService.findAllByUserId(pageable,userId));
    }

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