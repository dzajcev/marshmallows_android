package com.dzaitsev.marshmallow.dto;

import java.io.Serializable;

public abstract class NsiItem implements Serializable {

    private boolean active = true;

    private String name;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
