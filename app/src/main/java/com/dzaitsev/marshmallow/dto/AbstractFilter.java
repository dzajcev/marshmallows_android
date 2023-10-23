package com.dzaitsev.marshmallow.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFilter<T> implements Serializable {
    private LocalDate start;
    private LocalDate end;
    private List<T> statuses=new ArrayList<>();

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public List<T> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<T> statuses) {
        this.statuses = statuses;
    }
}
