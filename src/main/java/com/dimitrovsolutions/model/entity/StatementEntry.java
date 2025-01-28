package com.dimitrovsolutions.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class StatementEntry {
    private final String id;
    private final String email;
    private final UUID walletId;
    private final String cardNumber;
    private final String amount;
    private final String currency;
    private final String operation;
    private final String status;
    private final LocalDateTime timeStamp;

    public StatementEntry(String id, String email, UUID walletId, String cardNumber, String amount, String currency, String operation, String status, LocalDateTime timeStamp) {
        this.id = id;
        this.email = email;
        this.walletId = walletId;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.currency = currency;
        this.operation = operation;
        this.status = status;
        this.timeStamp = timeStamp;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getOperation() {
        return operation;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "StatementEntry{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", walletId=" + walletId +
                ", cardNumber='" + cardNumber + '\'' +
                ", amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                ", operation='" + operation + '\'' +
                ", status='" + status + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}