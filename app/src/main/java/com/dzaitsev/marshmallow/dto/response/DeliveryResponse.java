package com.dzaitsev.marshmallow.dto.response;

import com.dzaitsev.marshmallow.dto.Delivery;

import java.util.ArrayList;
import java.util.List;


public class DeliveryResponse {

    private List<Delivery> deliveries = new ArrayList<>();

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<Delivery> deliveries) {
        this.deliveries = deliveries;
    }
}
