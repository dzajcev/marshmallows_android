package com.dzaitsev.marshmallow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryStatus implements Comparable<DeliveryStatus> {
    NEW("Новая"),
    IN_PROGRESS("В процессе"),
    DONE("Выполнена");
    private final String text;
}
