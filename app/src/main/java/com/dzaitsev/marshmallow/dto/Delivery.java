package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Delivery implements Serializable, Cloneable {

    private Integer id;
    private LocalDateTime createDate;
    private User executor;

    private User createUser;
    private LocalDate deliveryDate;
    private LocalTime start;
    private LocalTime end;
    private List<Order> orders;

    private DeliveryStatus deliveryStatus;

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

    public User getExecutor() {
        return executor;
    }

    public void setExecutor(User executor) {
        this.executor = executor;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public User getCreateUser() {
        return createUser;
    }

    public void setCreateUser(User createUser) {
        this.createUser = createUser;
    }

    public boolean isMy() {
        return executor.getId().equals(createUser.getId());
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
        return Objects.hash(deliveryDate, start, end,
                getOrders().stream().sorted(Comparator.comparingInt(Order::hashCode)).collect(Collectors.toList()));
    }

    @NonNull
    @Override
    public Delivery clone() {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), Delivery.class);
    }
}
