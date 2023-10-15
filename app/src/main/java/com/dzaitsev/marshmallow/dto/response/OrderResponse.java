package com.dzaitsev.marshmallow.dto.response;

import com.dzaitsev.marshmallow.dto.Order;

import java.util.List;

public class OrderResponse {

    private List<Order> orders;

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
