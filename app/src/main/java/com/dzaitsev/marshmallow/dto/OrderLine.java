package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class OrderLine implements Serializable, Cloneable {

    private Integer id;

    private Integer num;

    private LocalDateTime createDate;

    private Good good;

    private Double price;

    private boolean done;

    private Integer count;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public Good getGood() {
        return good;
    }

    public void setGood(Good good) {
        this.good = good;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderLine orderLine = (OrderLine) o;
        return Objects.equals(num, orderLine.num) && Objects.equals(good, orderLine.good)
                && Objects.equals(price, orderLine.price) && Objects.equals(done, orderLine.done)
                && Objects.equals(count, orderLine.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, good, price, done, count);
    }

    @NonNull
    @Override
    public OrderLine clone() {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), OrderLine.class);
    }
}
