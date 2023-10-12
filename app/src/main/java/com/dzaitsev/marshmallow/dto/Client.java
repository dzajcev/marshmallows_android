package com.dzaitsev.marshmallow.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Client implements Serializable {
    private Integer id;

    private String name;

    private LocalDateTime createDate;

    private String defaultDeliveryAddress;

    private String phone;

    private List<LinkChannel> linkChannels;

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
}
