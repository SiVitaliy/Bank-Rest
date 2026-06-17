package com.example.bankcards.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(long id){
        super("Транзакция с "+id+" не найдена.");
    }
}
