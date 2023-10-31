package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OrderRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Order, OrderRecyclerViewAdapter.RecycleViewHolder> {
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");


    @Override
    public void onBindViewHolder(@NonNull RecycleViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (getShowItems().get(position).getOrderStatus() == OrderStatus.DONE) {
            holder.changeBackgroundTintColor(ContextCompat.getColor(holder.getView().getContext(), R.color.light_green));
        }
        if (getShowItems().get(position).getOrderStatus() == OrderStatus.SHIPPED) {
            holder.changeBackgroundTintColor(ContextCompat.getColor(holder.getView().getContext(), R.color.green));
        }
    }

    public static class RecycleViewHolder extends AbstractRecyclerViewHolder<Order> {
        private final TextView id;
        private final TextView clientName;
        private final TextView deadline;
        private final TextView createDate;
        private final TextView toPay;
        private final TextView status;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.orderItemId);
            clientName = itemView.findViewById(R.id.orderItemClientName);
            deadline = itemView.findViewById(R.id.orderItemDeadline);
            createDate = itemView.findViewById(R.id.orderItemDateCreate);
            toPay = itemView.findViewById(R.id.orderItemToPay);
            status = itemView.findViewById(R.id.ordertemStatus);
        }

        @Override
        public void bind(Order item) {
            super.bind(item);
            id.setText(String.format("#%s", getItem().getId()));
            clientName.setText(getItem().getClient().getName());
            deadline.setText(dateTimeFormatter.format(getItem().getDeadline()));
            createDate.setText(dateTimeFormatter.format(getItem().getCreateDate()));
            double sumOrder = getItem().getOrderLines().stream()
                    .mapToDouble(m -> m.getPrice() * m.getCount()).sum();
            toPay.setText(MoneyUtils.getInstance()
                    .moneyWithCurrencyToString(sumOrder - Optional.ofNullable(getItem().getPrePaymentSum()).orElse(0d)));
            status.setText(getItem().getOrderStatus().getText());
        }
    }

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }


}