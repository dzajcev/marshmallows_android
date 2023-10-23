package com.dzaitsev.marshmallow.dto.response;

import com.dzaitsev.marshmallow.dto.NsiItem;

import java.util.ArrayList;
import java.util.List;

public abstract class NsiResponse<T extends NsiItem> {
    private List<T> items = new ArrayList<>();

    public List<T> getItems() {
        return items;
    }

    public void setClients(List<T> items) {
        this.items = items;
    }
}
