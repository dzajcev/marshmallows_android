package com.dzaitsev.marshmallow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LinkChannel {
    PHONE(0),
    SMS(1),
    WHATSAPP(2);

    private final int idx;

}
