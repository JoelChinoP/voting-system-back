package com.users.exception;

public class CandidateAlreadyExistsException extends RuntimeException {
    public CandidateAlreadyExistsException(String message) {
        super(message);
    }
}
