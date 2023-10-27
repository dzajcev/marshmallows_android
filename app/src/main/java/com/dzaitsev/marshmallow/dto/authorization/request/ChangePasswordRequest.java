package com.dzaitsev.marshmallow.dto.authorization.request;


public class ChangePasswordRequest {
    private final String password;

    public ChangePasswordRequest(String password) {
        this.password = password;
    }
}
