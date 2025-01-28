package com.dimitrovsolutions.model.entity;

import java.util.UUID;

public class User {
    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String egn;
    private final String email;
    private final String password;
    private final UUID accountId;

    public User(UUID id, String firstName, String lastName, String egn, String email, String password, UUID accountId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.egn = egn;
        this.email = email;
        this.password = password;
        this.accountId = accountId;
    }

    public UUID getId() {
        return id;
    }

    public String getEgn() {
        return egn;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UUID getAccountId() {
        return accountId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", accountId=" + accountId +
                '}';
    }
}