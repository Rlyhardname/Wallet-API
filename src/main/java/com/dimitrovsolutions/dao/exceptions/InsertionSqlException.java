package com.dimitrovsolutions.dao.exceptions;

public class InsertionSqlException extends RuntimeException {
    public InsertionSqlException() {
        super("Error inserting user");
    }

    public InsertionSqlException(String message) {
        super(message);
    }
}