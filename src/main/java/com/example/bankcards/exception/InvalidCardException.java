package com.example.bankcards.exception;

public class InvalidCardException extends RuntimeException {
    public InvalidCardException(){
        super("Некорректные данные карты.");
    }
}
