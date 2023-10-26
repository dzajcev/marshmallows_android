package com.dzaitsev.marshmallow.dto;

public class ErrorDto {
    private final ErrorCodes code;

    public ErrorDto(ErrorCodes code) {
        this.code = code;
    }

    public ErrorCodes getCode() {
        return code;
    }

}
