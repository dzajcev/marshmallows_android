package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.dto.response.Price;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Good extends NsiItem implements Cloneable {
    private Integer id;

    private String description;
    private Double price;

    private final List<Price> prices = new ArrayList<>();

    private List<Attachment> images=new ArrayList<>();

    public List<Price> getPrices() {
        return prices;
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

    public List<Attachment> getImages() {
        return images;
    }

    public void setImages(List<Attachment> images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Good good = (Good) o;
        return Objects.equals(id, good.id) && Objects.equals(getName(), good.getName()) && Objects.equals(price, good.price)
                && Objects.equals(images, good.images);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getName(), price, images);
    }

    @NonNull
    @Override
    public Good clone() {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), Good.class);
    }
}
