package com.dimitrovsolutions.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WithdrawRequest {
    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("firstName")
    private String cardHolderFirstName;

    @JsonProperty("lastName")
    private String cardHolderLastName;

    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("expiryDate")
    private String expiryDate;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("amount")
    private Double amount;

    public WithdrawRequest() {

    }

    public WithdrawRequest(String email, String password, String cardHolderFirstName,
                           String cardHolderLastName, String cardNumber,
                           String expiryDate, String currency, Double amount) {
        this.email = email;
        this.password = password;
        this.cardHolderFirstName = cardHolderFirstName;
        this.cardHolderLastName = cardHolderLastName;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.currency = currency;
        this.amount = amount;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCardHolderFirstName() {
        return cardHolderFirstName;
    }

    public String getCardHolderLastName() {
        return cardHolderLastName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCurrency() {
        return currency;
    }

    public Double getAmount() {
        return amount;
    }
}