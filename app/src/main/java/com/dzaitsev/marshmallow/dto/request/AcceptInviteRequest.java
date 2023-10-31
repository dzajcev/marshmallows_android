package com.dzaitsev.marshmallow.dto.request;

public class AcceptInviteRequest {
    private final Integer requestId;

    public AcceptInviteRequest(Integer requestId) {
        this.requestId = requestId;
    }
}