package com.example.bankcards.controller;

import com.example.bankcards.dto.request.TransactionRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.TransactionDto;
import com.example.bankcards.entity.User;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
/**
 * REST-контроллер для пользовательских операций с транзакциями.
 *
 * Предоставляет текущему аутентифицированному пользователю возможность
 * просматривать свои транзакции, получать транзакцию по идентификатору
 * и выполнять перевод средств между картами.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/transactions")
@Tag(name = "Транзакции пользователя",
        description = "Просмотр и выполнение транзакций текущего пользователя")
public class TransactionController {
    private final TransactionService transactionService;
    /**
     * Возвращает страницу транзакций текущего пользователя.
     *
     * Поддерживает пагинацию и сортировку. Пользователь получает только те
     * транзакции, которые относятся к его картам.
     *
     * @param pageable параметры пагинации и сортировки
     * @param user текущий аутентифицированный пользователь
     * @return страница с транзакциями текущего пользователя
     */
    @Operation(summary = "Получить транзакции пользователя",
            description = "Возвращает страницу транзакций текущего пользователя с поддержкой пагинации и сортировки")
    @GetMapping()
    public ResponseEntity<PageResponse<TransactionDto>> findAllTransactionsForUser(
            @ParameterObject
            @PageableDefault(
                    size = 5,
                    sort = "creationTime",
                    direction = Sort.Direction.ASC
            )Pageable pageable,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User user){
        log.debug("Получение транзакций пользователем");
        return ResponseEntity.ok(transactionService.findAllByUser(pageable,user));
    }
    /**
     * Возвращает транзакцию текущего пользователя по идентификатору.
     *
     * Доступ разрешён только к транзакциям, связанным с картами текущего пользователя.
     *
     * @param id идентификатор транзакции
     * @param user текущий аутентифицированный пользователь
     * @return данные найденной транзакции
     */
    @Operation(summary = "Получить транзакцию",
            description = "Возвращает информацию о транзакции текущего пользователя по её идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> findTransactionForUser(
            @Parameter(description = "Идентификатор транзакции", example = "15")
            @PathVariable Long id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User user){
        log.debug("Получение транзакции пользователем");

        return ResponseEntity.ok(transactionService.getTransactionForUser(id,user));
    }
    /**
     * Выполняет перевод средств между картами текущего пользователя.
     *
     * После успешного выполнения операции создаётся транзакция, а в заголовок ответа
     * Location добавляется URI созданной транзакции.
     *
     * @param request данные для выполнения перевода
     * @param user текущий аутентифицированный пользователь
     * @return созданная транзакция
     */
    @Operation(summary = "Выполнить транзакцию",
            description = "Выполняет перевод между картами и возвращает созданную транзакцию")
    @PostMapping()
    public ResponseEntity<TransactionDto> performTransaction(
            @RequestBody TransactionRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User user){
        log.debug("Выполнение транзакций пользователем с ID {}", user.getId());

        TransactionDto transactionDto = transactionService.performTransaction(request,user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transactionDto.id())
                .toUri();
        return ResponseEntity.created(location)
                .body(transactionDto);
    }

}