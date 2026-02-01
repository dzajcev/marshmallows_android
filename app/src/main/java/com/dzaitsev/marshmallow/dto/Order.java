package com.dzaitsev.marshmallow.dto;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order implements Serializable, Cloneable {

    private Integer id;

    private LocalDateTime createDate;

    private LocalDate deadline;

    private String comment;

    private String deliveryAddress;

    private String phone;

    private boolean needDelivery;

    private Client client;

    @Builder.Default
    private List<OrderLine> orderLines=new ArrayList<>();

    private Double prePaymentSum;

    private Double paySum;

    private LocalDateTime completeDate;

    private OrderStatus orderStatus;

    private boolean clientNotificated;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(deadline, order.deadline) && Objects.equals(comment, order.comment)
                && Objects.equals(deliveryAddress, order.deliveryAddress) && Objects.equals(phone, order.phone)
                && Objects.equals(needDelivery, order.needDelivery) && Objects.equals(client, order.client)
                && getOrderLines().stream().sorted(Comparator.comparing(OrderLine::getId)).collect(Collectors.toList())
                .equals(order.getOrderLines().stream().sorted(Comparator.comparing(OrderLine::getId)).collect(Collectors.toList()))
                && Objects.equals(prePaymentSum, order.prePaymentSum)
                && Objects.equals(paySum, order.paySum)
                && Objects.equals(orderStatus, order.orderStatus) && Objects.equals(completeDate, order.completeDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deadline, comment, deliveryAddress, phone, needDelivery, client,
                getOrderLines().stream().sorted(Comparator.comparingInt(OrderLine::hashCode)).collect(Collectors.toList()),
                prePaymentSum, paySum, orderStatus, completeDate);
    }

    @NonNull
    @Override
    public Order clone() {
        Gson gson = GsonExt.getGson();
        return gson.fromJson(gson.toJson(this), Order.class);
    }
}
