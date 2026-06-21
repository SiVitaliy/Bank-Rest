package com.example.bankcards.exception;
/**
 * Исключение, выбрасываемое при обращении к несуществующей банковской карте.
 */
public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(Long id) {
        super("Карта с id "+id+" не найдена.");
    }
}
