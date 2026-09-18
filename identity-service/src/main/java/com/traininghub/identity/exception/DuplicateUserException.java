package com.traininghub.identity.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String field) { super("User already exists with " + field); }
}