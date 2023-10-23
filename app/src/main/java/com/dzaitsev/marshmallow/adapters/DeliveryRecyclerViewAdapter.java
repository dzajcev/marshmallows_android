package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.DeliveryStatus;
import com.dzaitsev.marshmallow.dto.Order;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Delivery, DeliveryRecyclerViewAdapter.RecycleViewHolder> {
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void onBindViewHolder(@NonNull RecycleViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (getShowItems().get(position).getStatus() == DeliveryStatus.DONE) {
            holder.changeBackgroundTintColor(ContextCompat.getColor(holder.getView().getContext(), R.color.green));
        }
        if (getShowItems().get(position).getStatus() == DeliveryStatus.IN_PROGRESS) {
            holder.changeBackgroundTintColor(ContextCompat.getColor(holder.getView().getContext(), R.color.light_green));
        }
    }

    public static class RecycleViewHolder extends AbstractRecyclerViewHolder<Delivery> {

        private final TextView id;
        private final TextView deliveryDate;
        private final TextView start;
        private final TextView end;
        private final TextView totalOrders;
        private final TextView deliveredOrders;

        private final TextView deliveryStatus;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.deliveryItemId);
            deliveryDate = itemView.findViewById(R.id.deliveryDate);
            start = itemView.findViewById(R.id.deliveryStart);
            end = itemView.findViewById(R.id.deliveryEnd);
            totalOrders = itemView.findViewById(R.id.totalOrders);
            deliveredOrders = itemView.findViewById(R.id.deliveredOrders);
            deliveryStatus = itemView.findViewById(R.id.deliveryStatus);
        }

        @Override
        public void bind(Delivery item) {
            super.bind(item);
            id.setText(String.format("#%s", getItem().getId()));
            deliveryDate.setText(dateTimeFormatter.format(item.getDeliveryDate()));
            start.setText(timeFormatter.format(item.getStart()));
            end.setText(timeFormatter.format(item.getEnd()));
            totalOrders.setText(String.format("%s", Optional.ofNullable(item.getOrders()).map(List::size).orElse(0)));
            deliveryStatus.setText(item.getStatus().getText());
            deliveredOrders.setText(String.format("%s", Optional.ofNullable(item.getOrders()).orElse(new ArrayList<>())
                    .stream().filter(Order::isShipped)
                    .count()));
        }
    }

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.delivery_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }


}