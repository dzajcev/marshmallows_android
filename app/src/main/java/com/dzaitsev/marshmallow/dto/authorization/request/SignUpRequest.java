package com.dzaitsev.marshmallow.dto.authorization.request;

import com.dzaitsev.marshmallow.dto.UserRole;

import java.util.ArrayList;
import java.util.List;


public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private UserRole role;

    public SignUpRequest() {
    }

    public SignUpRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}