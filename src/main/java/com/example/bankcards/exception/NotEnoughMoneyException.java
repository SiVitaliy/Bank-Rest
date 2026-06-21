package com.example.bankcards.exception;
/**
 * Исключение, выбрасываемое при попытке выполнить операцию
 * при недостаточном количестве средств.
 */
public class NotEnoughMoneyException extends RuntimeException{
    public NotEnoughMoneyException(){
        super("Недостаточно средств.");
    }
}
