package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.GsonExt;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;
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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Delivery implements Serializable, Cloneable {

    private Integer id;
    private LocalDateTime createDate;
    private User executor;

    private User createUser;
    private LocalDate deliveryDate;
    private LocalTime start;
    private LocalTime end;
    private List<Order> orders=new ArrayList<>();

    private DeliveryStatus deliveryStatus;

    public boolean isMy() {
        return createUser == null || AuthorizationHelper.getInstance().getUserData()
                .map(User::getId)
                .filter(f -> f.equals(createUser.getId()))
                .isPresent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delivery delivery = (Delivery) o;
        return Objects.equals(deliveryDate, delivery.deliveryDate)
                && Objects.equals(executor, delivery.executor)
                && Objects.equals(start, delivery.start) && Objects.equals(end, delivery.end)
                && new HashSet<>(getOrders()).equals(new HashSet<>(delivery.getOrders()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryDate, start, end, executor,
                getOrders().stream().sorted(Comparator.comparingInt(Order::hashCode)).collect(Collectors.toList()));
    }

    @NonNull
    @Override
    public Delivery clone() {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), Delivery.class);
    }
}
