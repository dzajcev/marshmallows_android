package com.dzaitsev.marshmallow.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Price implements Serializable {
    private Integer id;

    private LocalDateTime createDate;

    private Double price;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }


    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
