package com.dimitrovsolutions.model.utils;

public class MissingWalletException extends RuntimeException {
    public MissingWalletException(String currency) {
        super("User doesn't own wallet with " + currency + " currency,404");
    }
}