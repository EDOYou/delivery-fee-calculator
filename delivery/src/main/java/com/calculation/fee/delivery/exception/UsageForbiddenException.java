package com.calculation.fee.delivery.exception;

public class UsageForbiddenException extends RuntimeException {
    public UsageForbiddenException(String message) {
        super(message);
    }
}