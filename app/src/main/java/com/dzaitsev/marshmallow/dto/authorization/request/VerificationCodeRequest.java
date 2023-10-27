package com.dzaitsev.marshmallow.dto.authorization.request;

public class VerificationCodeRequest {
    private final String code;

    public VerificationCodeRequest(String code) {
        this.code = code;
    }
}