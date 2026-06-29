package com.codibly.energyapi.exception;

public class InsufficientGenerationDataException extends RuntimeException {

    public InsufficientGenerationDataException(String message) {
        super(message);
    }
}