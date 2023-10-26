package com.dzaitsev.marshmallow.dto.request;


public class SaveMyInfoRequest {
    private final String firstName;
    private final String lastName;

    public SaveMyInfoRequest(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
