package com.dimitrovsolutions.model.entity;

import java.util.UUID;

public class Wallet {
    private final UUID id;
    private final UUID accountId;
    private final String value;
    private final String currency;

    public Wallet(UUID id, UUID accountId, String value, String currency) {
        this.id = id;
        this.accountId = accountId;
        this.value = value;
        this.currency = currency;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    public static Wallet dummyWallet() {
        return new Wallet(null, null, null, null);
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", value='" + value + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}