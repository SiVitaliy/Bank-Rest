package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
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

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/users")
@Tag(name = "Администрирование пользователей",
        description = "Просмотр пользователей, их поиск, блокировка и разблокировка")
public class AdminUserController {
    private final UserService userService;

    @Operation(summary = "Получить список пользователей",
            description = "Возвращает страницу пользователей с поддержкой поиска, пагинации и сортировки")
    @GetMapping
    public ResponseEntity<PageResponse<UserDto>>findAllUsers(
            @ParameterObject
            @PageableDefault(size = 5, sort = "creationTime", direction = Sort.Direction.ASC)Pageable pageable,
            @Parameter(description = "Строка поиска пользователей", example = "ivan")
            @RequestParam(required = false) String search){
        log.debug("Получение списка пользователей");

        return ResponseEntity.ok(userService.findAll(pageable,search));
    }

    @Operation(summary = "Получить пользователя",
            description = "Возвращает информацию о пользователе по его идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findUser(
            @Parameter(description = "Идентификатор пользователя", example = "3")
            @PathVariable Long id){
        log.debug("Получение пользователя с ID {}",id);

        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "Заблокировать пользователя",
            description = "Блокирует пользователя по идентификатору"
    )
    @PatchMapping("/{userId}/lock")
    public ResponseEntity<UserDto> lock(
            @Parameter(description = "Идентификатор блокируемого пользователя", example = "3")
            @PathVariable Long userId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User admin){
        log.debug("Блокировка пользователя с ID {}",userId);

        return ResponseEntity.ok(userService.lock(userId,admin));
    }

    @Operation(summary = "Разблокировать пользователя",
            description = "Разблокирует пользователя по идентификатору")
    @PatchMapping("/{userId}/unlock")
    public ResponseEntity<UserDto> unlock(
            @Parameter(description = "Идентификатор разблокируемого пользователя", example = "3")
            @PathVariable Long userId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User admin){
        log.debug("Разблокировка пользователя с ID {}",userId);
        return ResponseEntity.ok(userService.unlock(userId,admin));
    }

}