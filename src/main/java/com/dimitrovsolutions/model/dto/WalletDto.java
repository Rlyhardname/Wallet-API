package com.dimitrovsolutions.model.dto;

public class WalletDto {
    private final String id;
    private final String value;
    private final String currency;

    public WalletDto(String id, String value, String currency) {
        this.id = id;
        this.value = value;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return "WalletDto{" +
                "accountId='" + id + '\'' +
                ", value='" + value + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}