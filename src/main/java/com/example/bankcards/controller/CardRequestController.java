package com.example.bankcards.controller;

import com.example.bankcards.dto.response.CardRequestDto;
import com.example.bankcards.dto.request.CardActionRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
@Tag(name = "Заявки на операции с картами",
        description = "Создание заявок на выпуск, блокировку, разблокировку и удаление карт")
public class CardRequestController {
    private final CardRequestService cardRequestService;

    @Operation(summary = "Создать заявку на операцию с картой",
            description = "Создаёт заявку на выполнение указанной операции с картой текущего пользователя")
    @PostMapping("/card-requests")
    public ResponseEntity<CardRequestDto> manageCard(
            @RequestBody CardActionRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User user){
        log.debug("Запрос на операцию с картой пользователем с ID {}",user.getId());

        return  ResponseEntity.ok(cardRequestService.save(request,user));
    }



}