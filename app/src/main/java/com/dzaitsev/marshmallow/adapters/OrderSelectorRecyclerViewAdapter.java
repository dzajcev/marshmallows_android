package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderSelectorRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Order, OrderSelectorRecyclerViewAdapter.RecycleViewHolder> {

    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Map<Integer, Boolean> selected = new HashMap<>();

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_selector_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }

    public void setItems(List<Order> items) {
        super.setItems(items);
    }

    public List<Order> getSelectedOrders() {
        return getOriginalItems().stream()
                .filter(f -> selected.getOrDefault(f.getId(), false))
                .collect(Collectors.toList());
    }

    public class RecycleViewHolder extends AbstractRecyclerViewHolder<Order> {
        private final TextView orderSelectorItemId;
        private final TextView orderSelectorClientName;
        private final TextView orderSelectorToPay;
        private final TextView orderSelectorDeliveryDate;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            orderSelectorItemId = itemView.findViewById(R.id.orderSelectorItemId);
            orderSelectorClientName = itemView.findViewById(R.id.orderSelectorClientName);
            orderSelectorToPay = itemView.findViewById(R.id.orderSelectorToPay);
            orderSelectorDeliveryDate = itemView.findViewById(R.id.orderSelectorDeliveryDate);


        }

        @Override
        public void bind(Order item) {
            super.bind(item);
            orderSelectorItemId.setText(String.format("#%s", item.getId()));
            orderSelectorClientName.setText(item.getClient().getName());
            orderSelectorToPay.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(calcToPay(item)));
            orderSelectorDeliveryDate.setText(dateTimeFormatter.format(getItem().getDeadline()));
            CheckBox orderSelectorSelect = itemView.findViewById(R.id.orderSelectorSelect);
            orderSelectorSelect.setOnCheckedChangeListener((buttonView, isChecked)
                    -> OrderSelectorRecyclerViewAdapter.this.selected.put(getItem().getId(), isChecked));
            orderSelectorSelect.setChecked(Boolean.TRUE.equals(selected.getOrDefault(getItem().getId(), false)));
        }

        private Double calcTotalSum(Order order) {
            return order.getOrderLines().stream()
                    .mapToDouble(m -> Optional.ofNullable(m.getPrice()).orElse(0d) * Optional.ofNullable(m.getCount()).orElse(0))
                    .sum();
        }

        private Double calcToPay(Order order) {
            return calcTotalSum(order) - (Optional.ofNullable(order.getPrePaymentSum()).orElse(0d)
                    + Optional.ofNullable(order.getPaySum()).orElse(0d));
        }
    }
}
