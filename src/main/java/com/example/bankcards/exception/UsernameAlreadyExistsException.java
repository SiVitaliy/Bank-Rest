package com.example.bankcards.exception;

public class UsernameAlreadyExistsException extends Throwable {
    public UsernameAlreadyExistsException(String s) {
        super(s);
    }
    public UsernameAlreadyExistsException() {
        super("Пользователь с таким именем уже существует.");
    }
}
