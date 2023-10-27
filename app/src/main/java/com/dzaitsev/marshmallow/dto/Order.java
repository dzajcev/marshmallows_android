package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class Order implements Serializable, Cloneable {

    private Integer id;

    private LocalDateTime createDate;

    private LocalDate deadline;

    private String comment;

    private String deliveryAddress;

    private String phone;

    private boolean needDelivery;

    private Client client;

    private List<OrderLine> orderLines;

    private Double prePaymentSum;

    private Double paySum;

    private boolean shipped;

    private LocalDateTime completeDate;

    private OrderStatus orderStatus;

    private boolean clientNotificated;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
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
        if (orderLines == null) {
            orderLines = new ArrayList<>();
        }
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isNeedDelivery() {
        return needDelivery;
    }

    public void setNeedDelivery(boolean needDelivery) {
        this.needDelivery = needDelivery;
    }

    public boolean isShipped() {
        return shipped;
    }

    public void setShipped(boolean shipped) {
        this.shipped = shipped;
    }

    public Double getPaySum() {
        return paySum;
    }

    public void setPaySum(Double paySum) {
        this.paySum = paySum;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public boolean isClientNotificated() {
        return clientNotificated;
    }

    public void setClientNotificated(boolean clientNotificated) {
        this.clientNotificated = clientNotificated;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(deadline, order.deadline) && Objects.equals(comment, order.comment)
                && Objects.equals(deliveryAddress, order.deliveryAddress) && Objects.equals(phone, order.phone)
                && Objects.equals(needDelivery, order.needDelivery) && Objects.equals(client, order.client)
                && new HashSet<>(getOrderLines()).equals(new HashSet<>(order.getOrderLines())) && Objects.equals(prePaymentSum, order.prePaymentSum)
                && Objects.equals(paySum, order.paySum)
                && Objects.equals(orderStatus, order.orderStatus) && Objects.equals(completeDate, order.completeDate);
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(deadline, comment, deliveryAddress, phone, needDelivery, client,
                getOrderLines().stream().sorted(Comparator.comparingInt(OrderLine::hashCode)).collect(Collectors.toList()),
                prePaymentSum, paySum, orderStatus, completeDate);
        return hash;
    }

    @NonNull
    @Override
    public Order clone() {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), Order.class);
    }
}
