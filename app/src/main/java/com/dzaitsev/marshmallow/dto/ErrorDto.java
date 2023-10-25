package com.dzaitsev.marshmallow.dto;

public class ErrorDto {
    private final ErrorCodes errorCode;
    private String message;

    public ErrorDto(ErrorCodes errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
