package com.dzaitsev.marshmallow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {

    IN_LINE("В очереди", true),
    IN_PROGRESS("В процессе", true),
    DONE("Выполнен", true),
    IN_DELIVERY("В доставке", false),
    SHIPPED("Доставлен", false),
    ISSUED("Выдан",false);


    private final String text;

    private final boolean isEditable;


    }
