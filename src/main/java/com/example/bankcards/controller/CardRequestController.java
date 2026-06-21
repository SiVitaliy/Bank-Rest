package com.example.bankcards.controller;

import com.example.bankcards.dto.response.CardRequestDto;
import com.example.bankcards.dto.request.CardActionRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
/**
 * REST-контроллер для создания пользовательских заявок на операции с картами.
 *
 * Позволяет текущему аутентифицированному пользователю создать заявку
 * на выпуск, блокировку, активацию или удаление банковской карты.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
@Tag(name = "Заявки на операции с картами",
        description = "Создание заявок на выпуск, блокировку, разблокировку и удаление карт")
public class CardRequestController {
    private final CardRequestService cardRequestService;
    /**
     * Создаёт заявку на выполнение операции с картой.
     *
     * Тип операции определяется значением поля requestType в теле запроса.
     * Для операций над существующей картой дополнительно передаётся идентификатор карты.
     *
     * @param request данные создаваемой заявки
     * @param user текущий аутентифицированный пользователь
     * @return созданная заявка на операцию с картой
     */
    @Operation(summary = "Создать заявку на операцию с картой",
            description = "Создаёт заявку на выполнение указанной операции с картой текущего пользователя")
    @PostMapping("/card-requests")
    public ResponseEntity<CardRequestDto> manageCard(
            @Valid @RequestBody CardActionRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User user){
        log.debug("Запрос на операцию с картой пользователем с ID {}",user.getId());

        return  ResponseEntity.ok(cardRequestService.save(request,user));
    }



}