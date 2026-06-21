package com.example.bankcards.exception;
/**
 * Исключение, выбрасываемое при обращении к несуществующей транзакции.
 */
public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(long id){
        super("Транзакция с "+id+" не найдена.");
    }
}
