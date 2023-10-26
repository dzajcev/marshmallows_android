package com.dzaitsev.marshmallow.dto.response;

import com.dzaitsev.marshmallow.dto.User;

public class UserInfoResponse {
   private final User user;

    public UserInfoResponse(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

}
