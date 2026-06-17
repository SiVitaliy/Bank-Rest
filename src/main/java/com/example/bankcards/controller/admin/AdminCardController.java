package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.filter.CardFilter;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateCardExpirationDateRequest;
import com.example.bankcards.dto.response.CardDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Админ: карты", description = "Управление картами любых пользователей")
@RequestMapping("/admin")
public class AdminCardController {

    private final CardService cardService;

    @Operation(summary = "Получить все карты",
            description = "Возвращает список всех карт с пагинацией и сортировкой по дате создания")
    @GetMapping("/cards")
    public ResponseEntity<PageResponse<CardDto>> findAllCards(
            @Valid @ParameterObject CardFilter filter,
            @ParameterObject
            @PageableDefault(
                    size = 5,
                    sort = "creationTime",
                    direction = Sort.Direction.ASC
            )Pageable pageable) {
        log.debug("Получение всех карт");
        return ResponseEntity.ok(cardService.findAllCards(filter,pageable));
    }

    @Operation(summary = "Получить карту по ID",
            description = "Возвращает конкретную карту по её идентификатору")
    @GetMapping("cards/{id}")
    public ResponseEntity<CardDto> findCard(
            @Parameter(description = "ID карты", example = "1") @PathVariable Long id) {
        log.debug("Получение карты по ID {}",id);
         return ResponseEntity.ok(cardService.findById(id));
    }

    @Operation(summary = "Создать карту для пользователя",
            description = "Создаёт новую банковскую карту и привязывает её к указанному пользователю")
    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<CardDto> createCard(
            @Parameter(description = "ID пользователя, которому выпускается карта", example = "3")
            @PathVariable("userId") Long userId,
            @Valid @RequestBody CreateCardRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        log.debug("Создание карты для пользователя с ID {}",userId);

        CardDto cardDto = cardService.save(userId, request, user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cardDto.id())
                .toUri();
        return ResponseEntity.created(location).body(cardDto);
    }

    @Operation(summary = "Изменить срок действия карты")
    @PatchMapping("/cards/{id}/expiration-date")
    public ResponseEntity<CardDto> setNewExpirationDate(
            @Parameter(description = "ID карты", example = "3") @PathVariable Long id,
            @Valid @RequestBody UpdateCardExpirationDateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        log.debug("Изменение срока действия карты");

        CardDto cardDto = cardService.setNewExpirationDate(id,request.expirationDate(), user);
        return ResponseEntity.ok(cardDto);
    }
}