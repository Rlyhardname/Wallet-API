package com.dimitrovsolutions.controller;

import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Global validator for everything atm.
 */
@Component
public class Validator {

    public void validateCredentials(String username, String password) {
        if (Objects.isNull(username) || Objects.isNull(password)) {
            throw new IllegalArgumentException("Email and password are required,400");
        }

        if (username.length() < 3) {
            throw new IllegalArgumentException("Email must be at least 3 characters,400");
        }

        if (password.length() < 8 || password.length() > 25) {
            throw new IllegalArgumentException("Password must be between 8 and 30 characters,400");
        }

        validatePassword(password);
    }

    private void validatePassword(String password) {
        int number = 0;
        int lowerCase = 0;
        int upperCase = 0;
        int specialSymbol = 0;
        for (int i = 0; i < password.length(); i++) {
            char currentChar = password.charAt(i);
            if (currentChar >= 48 && currentChar <= 57) {
                number++;
                continue;
            }

            if (currentChar >= 65 && currentChar <= 90) {
                upperCase++;
                continue;
            }

            if (currentChar >= 97 && currentChar <= 122) {
                lowerCase++;
            }

            if (currentChar >= 33 && currentChar <= 47) {
                specialSymbol++;
            }
        }

        if (number == 0 || lowerCase == 0 || upperCase == 0 || specialSymbol == 0) {
            throw new IllegalArgumentException("Password requires one special symbol," +
                    "one lower case character, one upper case character and one number,400");
        }
    }

    public void validateCurrency(String currencyCode) {
        if (Objects.isNull(currencyCode)) {
            throw new IllegalArgumentException("Currency code is required,400");
        }

        if (currencyCode.length() != 3) {
            throw new IllegalArgumentException("Currency code must be 3 characters,400");
        }
    }

    public void validateEgn(String egn) {
        if (Objects.isNull(egn)) {
            throw new IllegalArgumentException("Currency code is required,400");
        }

        if (egn.length() != 10) {
            throw new IllegalArgumentException("Currency code must be exactly 10 characters,400");
        }

        try {
            List<String> dateAndLastFourDigits = new ArrayList<>();
            for (int i = 0; i < 6; i += 2) {
                dateAndLastFourDigits.add(egn.substring(i, i + 2));
            }

            for (int i = 6; i < egn.length(); i++) {
                Integer.parseInt(egn.substring(i, i + 1));
            }

            int day = Integer.parseInt(dateAndLastFourDigits.get(2));
            int month = Integer.parseInt(dateAndLastFourDigits.get(1));

            String yearPrefix = month > 12 ? "20" : "19";
            int year = Integer.parseInt(yearPrefix + dateAndLastFourDigits.get(0));

            LocalDate.of(year, month, day);
        } catch (DateTimeException | NumberFormatException e) {
            throw new IllegalArgumentException("Illegal EGN.400");
        }
    }

    public void validateNames(String firstName, String lastName) {
        if (Objects.isNull(firstName) || Objects.isNull(lastName)) {
            throw new IllegalArgumentException("Both names are required is required,400");
        }

        if (firstName.length() < 2 || lastName.length() < 2) {
            throw new IllegalArgumentException("Names must be at least 2 characters,400");
        }

        if (firstName.length() > 32 && lastName.length() > 32) {
            throw new IllegalArgumentException("Names name must be at most 32 characters,400");
        }
    }

    public void validateAmount(Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive,400");
        }

        if (amount > 10000) {
            throw new IllegalArgumentException("Amount must be less than 10000,400");
        }
    }

    public void validateCard(String cardHolderFirstName, String cardHolderLastName,
                             String cardNumber, String csc, String expiryDate) {
        if (Objects.isNull(cardHolderFirstName) || Objects.isNull(cardHolderLastName)) {
            throw new IllegalArgumentException("Card holder first name and last name are required,400");
        }

        validateExpiryDate(expiryDate);

        validateCardNumber(cardNumber);

        validateCSC(csc);
    }

    private void validateExpiryDate(String expiryDate) {
        if (Objects.isNull(expiryDate)) {
            throw new IllegalArgumentException("Card expiry date is required,400");
        }

        LocalDate expire;
        try {
            String dateRemovedSlashes = expiryDate.replace("/", "");
            int day = Integer.parseInt(dateRemovedSlashes.substring(0, 2));
            int month = Integer.parseInt(dateRemovedSlashes.substring(2, 4));
            int year = Integer.parseInt(dateRemovedSlashes.substring(4, 8));

            expire = LocalDate.of(year, month, day);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Card expiry date must be a number,400");
        }

        if (expire.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("card has expired,400");
        }
    }

    private void validateCardNumber(String cardNumber) {
        if (Objects.isNull(cardNumber)) {
            throw new IllegalArgumentException("Card number is required,400");
        }

        String cardRemovedDashes = cardNumber.replace("-", "");
        String cardStripWhiteSpaces = cardRemovedDashes.strip();
        if (cardStripWhiteSpaces.length() < 12 || cardStripWhiteSpaces.length() > 19) {
            throw new IllegalArgumentException("Card number must be between 12 and 19 characters,400");
        }

        validateStringOfDigits(cardStripWhiteSpaces);
    }

    public void validateCSC(String csc) {
        if (Objects.isNull(csc)) {
            throw new IllegalArgumentException("CSC is required is required,400");
        }

        if (!csc.equals("withdraw")) {
            if (csc.length() < 3 || csc.length() > 4) {
                throw new IllegalArgumentException("CSC must be between 3 and 4 characters,400");
            }

            validateStringOfDigits(csc);
        }
    }

    private void validateStringOfDigits(String stringOfDigits) {
        for (int i = 0; i < stringOfDigits.length(); i++) {
            char currentChar = stringOfDigits.charAt(i);
            if (currentChar < 48 || currentChar > 57) {
                String value = stringOfDigits.length() > 4 ? "Card number" : "CSC";
                throw new IllegalArgumentException(
                        String.format("%s must contain only numeric digits ,400", value));
            }
        }
    }
}