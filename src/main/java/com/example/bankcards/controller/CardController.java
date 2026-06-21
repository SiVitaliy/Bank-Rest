package com.example.bankcards.controller;


import com.example.bankcards.dto.filter.CardFilter;
import com.example.bankcards.dto.request.ChangeBalanceRequest;
import com.example.bankcards.dto.response.CardDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * REST-контроллер для операций пользователя с банковскими картами.
 *
 * Предоставляет текущему аутентифицированному пользователю возможность
 * просматривать свои карты, получать информацию о конкретной карте
 * и выполнять операции изменения баланса.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/cards")
@Tag(name = "Карты пользователя",
        description = "Просмотр карт пользователя и выполнение операций с балансом")
public class CardController {
    private final CardService cardService;
    /**
     * Возвращает страницу карт текущего пользователя.
     *
     * Поддерживает фильтрацию по параметрам карты, пагинацию и сортировку.
     * Пользователь получает только карты, принадлежащие ему.
     *
     * @param filter параметры фильтрации карт
     * @param pageable параметры пагинации и сортировки
     * @param user текущий аутентифицированный пользователь
     * @return страница с картами текущего пользователя
     */
    @Operation(summary = "Получить карты пользователя",
            description = "Возвращает страницу карт текущего пользователя с поддержкой фильтрации, пагинации и сортировки")
    @GetMapping
    public ResponseEntity<PageResponse<CardDto>> findAllCardsForUser(
            @Valid @ParameterObject CardFilter filter,
            @ParameterObject
            @PageableDefault(size = 5, sort = "creationTime",
                    direction = Sort.Direction.ASC
            )Pageable pageable,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user
    ){
        log.debug("Получение карт пользователя с ID {}",user.getId());

        return ResponseEntity.ok(cardService.findAllCardsForUser(filter,pageable,user));
    }
    /**
     * Возвращает карту текущего пользователя по идентификатору.
     *
     * Доступ разрешён только к карте, принадлежащей текущему пользователю.
     *
     * @param id идентификатор карты
     * @param user текущий аутентифицированный пользователь
     * @return данные найденной карты
     */
    @Operation(summary = "Получить карту",
            description = "Возвращает информацию о карте по её идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<CardDto> findById(
            @Parameter(description = "Идентификатор карты", example = "3")
            @PathVariable Long id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user){
        log.debug("Получение карты пользователя с ID {}",user.getId());

        return ResponseEntity.ok(cardService.findByIdForUser(id,user));
    }
    /**
     * Изменяет баланс карты текущего пользователя.
     *
     * В зависимости от значения поля operation выполняет пополнение баланса
     * или списание средств с карты.
     *
     * @param id идентификатор карты
     * @param request данные операции изменения баланса
     * @param user текущий аутентифицированный пользователь
     * @return карта с обновлённым балансом
     * @throws IllegalArgumentException если передан неподдерживаемый тип операции
     */
    @Operation(summary = "Изменить баланс карты",
            description = "Пополняет баланс карты или списывает средства в зависимости от значения operation")
    @PatchMapping("/{id}/balance")
    public ResponseEntity<CardDto> addBalance(
            @Parameter(description = "Идентификатор карты", example = "3")
            @PathVariable Long id,

            @Valid @RequestBody ChangeBalanceRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User user
    ){
        log.debug("Изменение баланса  карты пользователя с ID {}",user.getId());

        if ("ADD".equals(request.operation())) {
            return ResponseEntity.ok(cardService.addBalance(id, request, user));
        }

        if ("SUBTRACT".equals(request.operation())) {
            return ResponseEntity.ok(cardService.subtractBalance(id, request, user));
        }
        throw new IllegalArgumentException("Некорректный ввод");

    }



}