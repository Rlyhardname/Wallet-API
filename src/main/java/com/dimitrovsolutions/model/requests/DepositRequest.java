package com.dimitrovsolutions.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DepositRequest {
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

    @JsonProperty("csc")
    private String csc;

    @JsonProperty("expiryDate")
    private String expiryDate;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("amount")
    private Double amount;

    public DepositRequest() {

    }

    public DepositRequest(String email, String password, String cardHolderFirstName, String cardHolderLastName,
                          String cardNumber, String csc, String expiryDate, String currency, Double amount) {
        this.email = email;
        this.password = password;
        this.cardHolderFirstName = cardHolderFirstName;
        this.cardHolderLastName = cardHolderLastName;
        this.cardNumber = cardNumber;
        this.csc = csc;
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

    public String getCsc() {
        return csc;
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