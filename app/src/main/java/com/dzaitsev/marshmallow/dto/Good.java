package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Objects;

public class Good implements Serializable, Cloneable {
    private Integer id;
    private String name;
    private Double price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Good good = (Good) o;
        return Objects.equals(id, good.id) && Objects.equals(name, good.name) && Objects.equals(price, good.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price);
    }

    @NonNull
    @Override
    public Good clone() {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(this), Good.class);
    }
}
