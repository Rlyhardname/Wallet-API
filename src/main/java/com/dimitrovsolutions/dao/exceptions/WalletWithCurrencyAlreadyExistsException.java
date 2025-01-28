package com.dimitrovsolutions.dao.exceptions;

public class WalletWithCurrencyAlreadyExistsException extends RuntimeException {
    public WalletWithCurrencyAlreadyExistsException(String currency) {
        System.out.println("Wallet with currency " + currency + " already exists");
    }
}