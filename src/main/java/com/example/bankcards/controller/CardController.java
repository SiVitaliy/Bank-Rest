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

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/cards")
@Tag(name = "Карты пользователя",
        description = "Просмотр карт пользователя и выполнение операций с балансом")
public class CardController {
    private final CardService cardService;

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

        return ResponseEntity.ok(cardService.findAllCardsFotUser(filter,pageable,user));
    }

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