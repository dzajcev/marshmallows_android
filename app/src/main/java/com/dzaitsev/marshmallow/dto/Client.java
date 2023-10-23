package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client extends NsiItem implements Cloneable {
    private Integer id;

    private LocalDateTime createDate;

    private String defaultDeliveryAddress;

    private String phone;

    private String comment;


    private List<LinkChannel> linkChannels = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public String getDefaultDeliveryAddress() {
        return defaultDeliveryAddress;
    }

    public void setDefaultDeliveryAddress(String defaultDeliveryAddress) {
        this.defaultDeliveryAddress = defaultDeliveryAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<LinkChannel> getLinkChannels() {
        return linkChannels;
    }

    public void setLinkChannels(List<LinkChannel> linkChannels) {
        this.linkChannels = linkChannels;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client client)) return false;
        return getName().equals(client.getName())
                && Objects.equals(defaultDeliveryAddress, client.defaultDeliveryAddress)
                && Objects.equals(phone, client.phone) && Objects.equals(comment, client.comment) && Objects.equals(linkChannels, client.linkChannels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), defaultDeliveryAddress, phone, comment, linkChannels);
    }

    @NonNull
    @Override
    public Client clone() {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), Client.class);
    }

}
