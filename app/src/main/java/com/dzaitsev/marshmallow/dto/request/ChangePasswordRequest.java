package com.dzaitsev.marshmallow.dto.request;


public class ChangePasswordRequest {
    private final String password;

    public ChangePasswordRequest(String password) {
        this.password = password;
    }
}
