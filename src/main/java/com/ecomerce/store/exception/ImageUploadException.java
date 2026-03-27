package com.ecomerce.store.exception;

public class ImageUploadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ImageUploadException(String message) {
        super(message);
    }

    public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
