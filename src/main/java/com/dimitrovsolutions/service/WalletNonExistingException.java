package com.dimitrovsolutions.service;

public class WalletNonExistingException extends RuntimeException {

    public WalletNonExistingException(String accountId, String currency) {
        super(String.format("Wallet with accountId of %s and currency %s does not exist", accountId, currency));
    }
}