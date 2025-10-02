package com.dzaitsev.marshmallow.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractFilter<T> implements Serializable {
    private LocalDate start;
    private LocalDate end;
    private List<T> statuses=new ArrayList<>();

}
