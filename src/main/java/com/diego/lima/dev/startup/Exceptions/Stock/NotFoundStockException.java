package com.diego.lima.dev.startup.Exceptions.Stock;

public class NotFoundStockException extends RuntimeException {
    public NotFoundStockException(String message) {
        super(message);
    }
}
