package com.dzaitsev.marshmallow.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.OrderLineSimpleRecyclerViewAdapter;
import com.dzaitsev.marshmallow.dto.OrderLine;

import java.util.Comparator;
import java.util.List;

public class OrderSimpleComponent extends ConstraintLayout {
    OrderLineSimpleRecyclerViewAdapter orderLineSimpleRecyclerViewAdapter;

    public OrderSimpleComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initControl(context);
    }

    public OrderSimpleComponent(Context context) {
        super(context);
        initControl(context);
    }

    private void initControl(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.order_simple, this);
        RecyclerView orderSimpleList = findViewById(R.id.orderSimpleList);
        orderSimpleList.setLayoutManager(new LinearLayoutManager(context));

        orderLineSimpleRecyclerViewAdapter = new OrderLineSimpleRecyclerViewAdapter();

        orderSimpleList.setAdapter(orderLineSimpleRecyclerViewAdapter);
    }

    public void setItems(List<OrderLine> items) {
        items.sort(Comparator.comparing(OrderLine::getNum));
        orderLineSimpleRecyclerViewAdapter.setItems(items);
    }
}
