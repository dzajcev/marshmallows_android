package com.dzaitsev.marshmallow.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InviteRequest extends NsiItem {

    private Integer id;
    private User user;

    private LocalDateTime createDate;
    private LocalDateTime acceptDate;

    private InviteRequestDirection direction;

}