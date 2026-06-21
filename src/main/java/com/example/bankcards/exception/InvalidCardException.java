package com.example.bankcards.exception;
/**
 * Исключение, выбрасываемое при получении некорректных данных банковской карты.
 */
public class InvalidCardException extends RuntimeException {
    public InvalidCardException(){
        super("Некорректные данные карты.");
    }
}
