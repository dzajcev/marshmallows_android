package com.dzaitsev.marshmallow.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Price implements Serializable {
    private Integer id;

    private LocalDateTime createDate;

    private Double price;

}
