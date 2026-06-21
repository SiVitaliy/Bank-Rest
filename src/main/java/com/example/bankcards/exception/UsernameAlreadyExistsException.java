package com.example.bankcards.exception;
/**
 * Исключение, выбрасываемое при попытке зарегистрировать пользователя
 * с уже существующим именем.
 */
public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String s) {
        super(s);
    }
    public UsernameAlreadyExistsException() {
        super("Пользователь с таким именем уже существует.");
    }
}
