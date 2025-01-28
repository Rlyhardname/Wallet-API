package com.dimitrovsolutions.dao.exceptions;

public class EmailTakenException extends RuntimeException {
    public EmailTakenException() {
        super("Email already in use");
    }
}