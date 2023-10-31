package com.dzaitsev.marshmallow.dto;

public class ErrorDto {
    private final ErrorCodes code;

    private String message;

    public ErrorDto(ErrorCodes code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorDto(ErrorCodes code) {
        this.code = code;
    }

    public ErrorCodes getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
