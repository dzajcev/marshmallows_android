package com.dzaitsev.marshmallow.dto.response;

import com.dzaitsev.marshmallow.dto.InviteRequest;

import java.util.ArrayList;
import java.util.List;


public class InviteRequestsResponse {
    private final List<InviteRequest> requests = new ArrayList<>();

    public List<InviteRequest> getRequests() {
        return requests;
    }
}
