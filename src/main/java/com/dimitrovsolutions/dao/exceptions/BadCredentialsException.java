package com.dimitrovsolutions.dao.exceptions;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException() {
        super("Wrong credentials, please try again");
    }
}
