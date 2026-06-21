package com.example.bankcards.exception.handler;

import com.example.bankcards.dto.error.ErrorResponseDto;
import com.example.bankcards.dto.error.FieldErrorDto;
import com.example.bankcards.dto.error.ValidationErrorResponse;
import com.example.bankcards.exception.*;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
/**
 * Глобальный обработчик исключений REST API.
 *
 * Преобразует доменные, валидационные и инфраструктурные исключения
 * в унифицированные HTTP-ответы с кодом ошибки и сообщением.
 */
@RestControllerAdvice
public class RestExceptionHandler {
    /**
     * Обрабатывает ошибку регистрации пользователя с уже существующим именем.
     *
     * @param ex исключение о существующем имени пользователя
     * @return ответ с HTTP-статусом 409 Conflict
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto("USER.USERNAME_ALREADY_EXISTS", ex.getMessage()));
    }

    /**
     * Обрабатывает обращение к несуществующей карте.
     *
     * @param ex исключение о ненайденной карте
     * @return ответ с HTTP-статусом 404 Not Found
     */
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCardNotFound(CardNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto("CARD.NOT_FOUND", ex.getMessage()));
    }

    /**
     * Обрабатывает ошибку некорректных данных карты.
     *
     * @param ex исключение о некорректной карте
     * @return ответ с HTTP-статусом 400 Bad Request
     */
    @ExceptionHandler(InvalidCardException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCard(InvalidCardException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("CARD.INVALID", ex.getMessage()));
    }

    /**
     * Обрабатывает ошибку недостаточного количества средств.
     *
     * @param ex исключение о недостаточном балансе
     * @return ответ с HTTP-статусом 409 Conflict
     */
    @ExceptionHandler(NotEnoughMoneyException.class)
    public ResponseEntity<ErrorResponseDto> handleNotEnoughMoney(NotEnoughMoneyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto("BALANCE.NOT_ENOUGH_MONEY", ex.getMessage()));
    }

    /**
     * Обрабатывает обращение к несуществующей заявке на операцию с картой.
     *
     * @param ex исключение о ненайденной заявке
     * @return ответ с HTTP-статусом 404 Not Found
     */
    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleRequestNotFound(RequestNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto("CARD_REQUEST.NOT_FOUND", ex.getMessage()));
    }

    /**
     * Обрабатывает обращение к несуществующей транзакции.
     *
     * @param ex исключение о ненайденной транзакции
     * @return ответ с HTTP-статусом 404 Not Found
     */
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleTransactionNotFound(TransactionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto("TRANSACTION.NOT_FOUND", ex.getMessage()));
    }

    /**
     * Обрабатывает обращение к несуществующему пользователю.
     *
     * @param ex исключение о ненайденном пользователе
     * @return ответ с HTTP-статусом 404 Not Found
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto("USER.NOT_FOUND", ex.getMessage()));
    }

    /**
     * Обрабатывает ошибку некорректного аргумента.
     *
     * @param ex исключение о некорректном аргументе
     * @return ответ с HTTP-статусом 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("DATA.INVALID_ARGUMENT", ex.getMessage()));
    }

    /**
     * Обрабатывает ошибку некорректного состояния операции.
     *
     * @param ex исключение о некорректном состоянии
     * @return ответ с HTTP-статусом 400 Bad Request
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("DATA.INVALID_ARGUMENT", ex.getMessage()));
    }

    /**
     * Обрабатывает ошибку неверных учётных данных при аутентификации.
     *
     * @param ex исключение о неверном логине или пароле
     * @return ответ с HTTP-статусом 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto("USER.BAD_CREDENTIALS", ex.getMessage()));
    }

    /**
     * Обрабатывает ошибки валидации тела запроса.
     *
     * Формирует список ошибок по полям, которые не прошли bean validation.
     *
     * @param ex исключение с результатами валидации
     * @return ответ с HTTP-статусом 400 Bad Request и списком ошибок по полям
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        List<FieldErrorDto> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDto(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(new ValidationErrorResponse(
                        "VALIDATION.ERROR",
                        "Запрос содержит некорректные данные",
                        errors
                ));
    }

    /**
     * Обрабатывает ошибку преобразования параметра запроса.
     *
     * Например, если вместо числа в path variable или query parameter
     * передано некорректное строковое значение.
     *
     * @param ex исключение о невозможности преобразовать параметр
     * @return ответ с HTTP-статусом 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(
                        "REQUEST.INVALID_PARAMETER",
                        "Некорректное значение параметра: " + ex.getName()
                ));
    }

    /**
     * Обрабатывает обращение к несуществующему URL.
     *
     * @param ex исключение о ненайденном ресурсе
     * @return ответ с HTTP-статусом 404 Not Found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(
                        "RESOURCE.NOT_FOUND",
                        "Запрашиваемый адрес не найден"
                ));
    }

    /**
     * Обрабатывает ошибку отсутствия прав доступа.
     *
     * @param ex исключение о запрете доступа
     * @return ответ с HTTP-статусом 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDto(
                        "ACCESS.DENIED",
                        "Недостаточно прав"
                ));
    }
    /**
     * Обрабатывает ошибку некорректных параметров запроса.
     *
     * @param ex исключение о некорректных параметрах.
     * @return ответ с HTTP-статусом 400 Bad Request
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(PropertyReferenceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("REQUEST.INVALID", ex.getMessage()
                ));
    }
}
