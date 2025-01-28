package com.dimitrovsolutions.model.component;

import org.springframework.stereotype.Component;

/**
 * Used to simulate communication with outside institution for send/request money transaction
 */
@Component
public class ExternalTransactionsUtil {

    /**
     * Simulating request to outside financial institution for withdrawing from client's account to deposit into wallet
     */
    public boolean requestMoneyFromCardIssuer(String firstName, String lastName, String egn, String statementId,
                                              String cardNumber, String CSC, String currency, double amount) {
        double ALLOWED_DEPOSIT_AMOUNT = 500.00;
        return amount < ALLOWED_DEPOSIT_AMOUNT;
    }

    /**
     * Simulating transaction failure on our server side, returning message to financial institution to reverse transaction
     * on their side.
     */
    public void reverseMoneyRequestFromCardIssuer(String firstName, String lastName, String egn, String statementId,
                                                  String cardNumber, String CSC, String currency, double amount) {

    }

    /**
     * Simulating transaction success, sending message to financial institution to complete their send transaction successfully.
     */
    public void confirmSuccessfulDepositFromCardIssuer(String firstName, String lastName, String egn, String statementId,
                                                       String cardNumber, String CSC, String currency, double amount) {

    }

    /**
     * Wire money to account linked to credit/debit card.
     */
    public boolean sendMoneyToCardIssuer(String firstName, String lastName, String egn, String statementId,
                                         String cardNumber, String currency, double amount) {

        // Async wait for response
        return true;
    }
}