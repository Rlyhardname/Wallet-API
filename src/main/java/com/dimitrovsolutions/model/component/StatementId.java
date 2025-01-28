package com.dimitrovsolutions.model.component;

import org.springframework.stereotype.Component;

/**
 * Generates statement id's for statement table
 */
@Component
public class StatementId {
    private String id;

    StatementId() {
        id = "0";
    }

    public synchronized String increment() {
        int result = Integer.parseInt(id) + 1;
        id = String.valueOf(result);
        return id;
    }
}