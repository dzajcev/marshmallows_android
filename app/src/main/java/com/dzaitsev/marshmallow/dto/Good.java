package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.dto.response.Price;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Good extends NsiItem implements Cloneable {
    private Integer id;

    private String description;
    private Double price;

    private final List<Price> prices = new ArrayList<>();

    private List<Attachment> images = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Good good = (Good) o;
        return Objects.equals(getId(), good.getId()) && Objects.equals(getName(), good.getName()) && Objects.equals(getPrice(), good.getPrice())
                && Objects.equals(getDescription(), good.getDescription())
                && Objects.equals(isActive(), good.isActive())
                && Objects.equals(new HashSet<>(getImages()), new HashSet<>(good.getImages()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getPrice(), getImages(), getDescription(), isActive());
    }

    @NonNull
    @Override
    public Good clone() throws CloneNotSupportedException {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), Good.class);
    }
}
