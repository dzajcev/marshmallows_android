package com.dzaitsev.marshmallow.dto;

public enum DeliveryStatus {
    NEW("Новая"),
    IN_PROGRESS("В процессе"),
    DONE("Выполнена");
    private final String text;

    DeliveryStatus(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
