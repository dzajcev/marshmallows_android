package com.dzaitsev.marshmallow.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class NsiItem implements Serializable {

    private boolean active = true;

    private String name;

}
