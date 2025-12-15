package com.dzaitsev.marshmallow.dto.bundles;

import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderLine;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCardBundle {
    private Order order;
    private List<OrderLine> orderLines;
    private int activeTab;
}
