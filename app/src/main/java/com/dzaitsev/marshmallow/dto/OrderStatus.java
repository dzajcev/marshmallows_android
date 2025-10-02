package com.dzaitsev.marshmallow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    IN_PROGRESS("В процессе"),
    DONE("Выполнен"),
    IN_DELIVERY("В доставке"),
    SHIPPED("Доставлен");

    private final String text;


}
