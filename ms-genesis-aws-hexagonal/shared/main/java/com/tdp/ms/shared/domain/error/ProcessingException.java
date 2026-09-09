package com.tdp.ms.shared.domain.error;

public class ProcessingException extends DomainException {
    public ProcessingException(String code, String message) {
        super(code, message);
    }
}
