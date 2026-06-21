package com.example.bankcards.exception;
/**
 * Исключение, выбрасываемое при обращении к несуществующей заявке
 * на операцию с банковской картой.
 */
public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(Long requestId) {
        super(" Запроса на операцию с картой с id = "+requestId+" не существует.");
    }
}
