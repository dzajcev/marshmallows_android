package com.dzaitsev.marshmallow.dto;

public enum OrderStatus {
    IN_PROGRESS("В процессе"),
    DONE("Выполнен"),
    SHIPPED("Доставлен");

    private String text;

    OrderStatus(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
