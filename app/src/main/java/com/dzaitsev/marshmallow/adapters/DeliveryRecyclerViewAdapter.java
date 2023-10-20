package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeliveryRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Delivery, DeliveryRecyclerViewAdapter.RecycleViewHolder> {
    private EditItemListener editItemListener;
    private SelectItemListener selectItemListener;
    private DeleteItemListener deleteItemListener;

    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public interface EditItemListener {
        void edit(Delivery item);
    }

    public interface SelectItemListener {
        void selectItem(Delivery item);
    }

    public interface DeleteItemListener {
        void deleteItem(Delivery item);
    }

    public void setEditItemListener(EditItemListener editItemListener) {
        this.editItemListener = editItemListener;
    }

    public void setSelectItemListener(SelectItemListener selectItemListener) {
        this.selectItemListener = selectItemListener;
    }

    public void setDeleteItemListener(DeleteItemListener deleteItemListener) {
        this.deleteItemListener = deleteItemListener;
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (getShowItems().get(position).getStatus() == DeliveryStatus.DONE) {
            holder.changeBackgroundTintColor(ContextCompat.getColor(holder.getView().getContext(), R.color.green));
        }
    }

    public class RecycleViewHolder extends AbstractRecyclerViewHolder<Delivery> {

        private final TextView id;
        private final TextView deliveryDate;
        private final TextView start;
        private final TextView end;
        private final TextView totalOrders;
        private final TextView deliveredOrders;
        private final ImageButton delete;
        private final ImageButton edit;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.deliveryItemId);
            deliveryDate = itemView.findViewById(R.id.deliveryDate);
            start = itemView.findViewById(R.id.deliveryStart);
            end = itemView.findViewById(R.id.deliveryEnd);
            totalOrders = itemView.findViewById(R.id.totalOrders);
            deliveredOrders = itemView.findViewById(R.id.deliveredOrders);
            View layout = itemView.findViewById(R.id.deliveryItemLayout);
            edit = itemView.findViewById(R.id.deliveryItemEdit);
            delete = itemView.findViewById(R.id.deliveryItemDelete);
            if (selectItemListener != null) {
                edit.setVisibility(View.GONE);
                layout.setOnClickListener(view -> selectItemListener.selectItem(getItem()));
            } else {
                edit.setOnClickListener(v -> {
                    if (editItemListener != null) {
                        editItemListener.edit(getItem());
                    }
                });
            }
        }

        @Override
        public void bind(Delivery item) {
            super.bind(item);
            id.setText(String.format("#%s", getItem().getId()));
            deliveryDate.setText(dateTimeFormatter.format(item.getDeliveryDate()));
            start.setText(timeFormatter.format(item.getStart()));
            end.setText(timeFormatter.format(item.getEnd()));
            totalOrders.setText(String.format("%s", Optional.ofNullable(item.getOrders()).map(List::size).orElse(0)));
            deliveredOrders.setText(String.format("%s", Optional.ofNullable(item.getOrders()).orElse(new ArrayList<>())
                    .stream().filter(Order::isShipped)
                    .collect(Collectors.toList()).size()));
            if (getItem().getStatus() == DeliveryStatus.NEW) {
                delete.setOnClickListener(v -> {
                    if (deleteItemListener != null) {
                        deleteItemListener.deleteItem(getItem());
                    }
                });
            } else {
                delete.setVisibility(View.GONE);
            }
        }

        protected List<View> getViewsForChangeColor() {
            return Stream.of(getView(), delete, edit).collect(Collectors.toList());
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