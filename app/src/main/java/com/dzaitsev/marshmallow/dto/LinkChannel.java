package com.dzaitsev.marshmallow.dto;

public enum LinkChannel {
    PHONE(0),
    SMS(1),
    WHATSAPP(2);


    private final int idx;

    LinkChannel(int idx) {
        this.idx = idx;
    }

    public int getIdx() {
        return idx;
    }
}
