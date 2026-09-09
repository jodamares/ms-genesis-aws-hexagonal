package com.tdp.ms.shared.domain.error;

public class InvalidInputException extends DomainException {
    public InvalidInputException(String code, String message) {
        super(code, message);
    }
}
