package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderLine implements Serializable, Cloneable {

    private Integer id;

    private Integer num;

    private LocalDateTime createDate;

    private Good good;

    private Double price;

    private boolean done;

    private Integer count;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderLine orderLine = (OrderLine) o;
        return Objects.equals(good, orderLine.good)
                && Objects.equals(price, orderLine.price) && Objects.equals(done, orderLine.done)
                && Objects.equals(count, orderLine.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(good, price, done, count);
    }

    @NonNull
    @Override
    public OrderLine clone() {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), OrderLine.class);
    }
}
