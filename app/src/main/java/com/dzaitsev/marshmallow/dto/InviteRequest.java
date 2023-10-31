package com.dzaitsev.marshmallow.dto;

import java.time.LocalDateTime;

public class InviteRequest extends NsiItem {

    private Integer id;
    private User user;

    private LocalDateTime createDate;
    private LocalDateTime acceptDate;

    private InviteRequestDirection direction;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(LocalDateTime acceptDate) {
        this.acceptDate = acceptDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public InviteRequestDirection getDirection() {
        return direction;
    }

    public void setDirection(InviteRequestDirection direction) {
        this.direction = direction;
    }
}