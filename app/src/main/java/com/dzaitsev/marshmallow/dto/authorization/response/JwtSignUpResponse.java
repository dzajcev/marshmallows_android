package com.dzaitsev.marshmallow.dto.authorization.response;

public class JwtSignUpResponse {
    private String token;
    private int verificationCodeTtl;

    public String getToken() {
        return token;
    }


    public int getVerificationCodeTtl() {
        return verificationCodeTtl;
    }

}