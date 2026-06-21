package com.example.bankcards.exception;
/**
 * Исключение, выбрасываемое при попытке добавить карту, которая уже существует.
 */
public class CardAlreadyExistsException extends RuntimeException{
    public CardAlreadyExistsException(){
        super("Произошла ошибка пр добавлении карты.");
    }

}
