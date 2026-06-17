package com.example.bankcards.exception;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(Long requestId) {
        super(" Запроса на операцию с картой с id = "+requestId+" не существует.");
    }
}
