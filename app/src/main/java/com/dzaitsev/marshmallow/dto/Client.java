package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Client extends NsiItem implements Cloneable {
    private Integer id;

    private LocalDateTime createDate;

    private String defaultDeliveryAddress;

    private String phone;

    private String comment;


    private List<LinkChannel> linkChannels = new ArrayList<>();

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
