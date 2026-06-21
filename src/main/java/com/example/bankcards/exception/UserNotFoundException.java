package com.example.bankcards.exception;
/**
 * Исключение, выбрасываемое при обращении к несуществующему пользователю.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String s) {
        super(s);
    }
    public UserNotFoundException(Long id ) {
        super("Пользователь с id "+id +"не найден.");
    }
}
