package com.dzaitsev.marshmallow.dto.request;

import com.dzaitsev.marshmallow.dto.UserRole;

import java.util.ArrayList;
import java.util.List;


public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private List<UserRole> roles = new ArrayList<>();

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

    public List<UserRole> getRoles() {
        return roles;
    }

}