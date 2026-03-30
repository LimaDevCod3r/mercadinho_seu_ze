package com.diego.lima.dev.startup.Exceptions.Product;

public class NotFoundProductException extends RuntimeException {
    public NotFoundProductException(String message) {
        super(message);
    }
}
