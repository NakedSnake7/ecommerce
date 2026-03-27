package com.ecomerce.store.exceptions;

public class OrderNotFoundException extends RuntimeException {
    
    // Agrega un serialVersionUID
    private static final long serialVersionUID = 1L;

    // Constructor
    public OrderNotFoundException(String message) {
        super(message);
    }
}
