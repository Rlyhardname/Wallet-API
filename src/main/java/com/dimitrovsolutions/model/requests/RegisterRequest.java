package com.dimitrovsolutions.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterRequest {
    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("egn")
    private String egn;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String egn, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.egn = egn;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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
}