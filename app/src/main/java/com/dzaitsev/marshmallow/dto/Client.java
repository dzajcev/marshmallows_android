package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client implements Serializable, Cloneable {
    private Integer id;

    private String name;

    private LocalDateTime createDate;

    private String defaultDeliveryAddress;

    private String phone;

    private String comment;

    private List<LinkChannel> linkChannels=new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
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
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return name.equals(client.name) && Objects.equals(defaultDeliveryAddress,
                client.defaultDeliveryAddress) && phone.equals(client.phone) && linkChannels.equals(client.linkChannels)
                && Objects.equals(comment, client.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, defaultDeliveryAddress, phone, linkChannels, comment);
    }

    @NonNull
    @Override
    public Client clone() {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), Client.class);
    }

}
