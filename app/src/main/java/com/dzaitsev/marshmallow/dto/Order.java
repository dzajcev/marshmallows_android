package com.dzaitsev.marshmallow.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Order implements Serializable {

    private Integer id;

    private LocalDateTime createDate;

    private LocalDate deadline;

    private LinkChannel linkChannel;

    private String comment;

    private String deliveryAddress;

    private Client client;

    private List<OrderLine> orderLines=new ArrayList<>();

    private Double prePaymentSum;

    private Boolean shipped;

    private LocalDateTime completeDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LinkChannel getLinkChannel() {
        return linkChannel;
    }

    public void setLinkChannel(LinkChannel linkChannel) {
        this.linkChannel = linkChannel;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<OrderLine> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
    }

    public Double getPrePaymentSum() {
        return prePaymentSum;
    }

    public void setPrePaymentSum(Double prePaymentSum) {
        this.prePaymentSum = prePaymentSum;
    }

    public Boolean getShipped() {
        return shipped;
    }

    public void setShipped(Boolean shipped) {
        this.shipped = shipped;
    }

    public LocalDateTime getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(LocalDateTime completeDate) {
        this.completeDate = completeDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
