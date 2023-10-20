package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Delivery implements Serializable, Cloneable {

    private Integer id;
    private LocalDateTime createDate;
    private LocalDate deliveryDate;
    private LocalTime start;
    private LocalTime end;
    private List<Order> orders;

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    public List<Order> getOrders() {
        if (orders == null) {
            orders = new ArrayList<>();
        }
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DeliveryStatus getStatus() {
        if (getOrders() != null && (getOrders().stream().allMatch(Order::isShipped))) {
            return DeliveryStatus.DONE;
        } else if (getOrders() != null && (getOrders().stream().anyMatch(f -> !f.isShipped())
                && getOrders().stream().anyMatch(Order::isShipped))) {
            return DeliveryStatus.IN_PROGRESS;
        } else {
            return DeliveryStatus.NEW;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delivery delivery = (Delivery) o;
        return Objects.equals(deliveryDate, delivery.deliveryDate)
                && Objects.equals(start, delivery.start) && Objects.equals(end, delivery.end)
                && new HashSet<>(getOrders()).equals(new HashSet<>(delivery.getOrders()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryDate, start, end, getOrders());
    }

    @NonNull
    @Override
    public Delivery clone() {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), Delivery.class);
    }
}
