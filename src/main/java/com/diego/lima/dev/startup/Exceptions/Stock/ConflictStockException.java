package com.diego.lima.dev.startup.Exceptions.Stock;

public class ConflictStockException extends RuntimeException {
    public ConflictStockException(String message) {
        super(message);
    }
}
