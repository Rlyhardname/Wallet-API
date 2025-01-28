package com.dimitrovsolutions.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CredentialsRequest {
    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    public CredentialsRequest() {
    }

    public CredentialsRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}