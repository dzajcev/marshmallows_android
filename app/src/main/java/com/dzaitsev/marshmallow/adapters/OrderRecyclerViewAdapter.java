package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OrderRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Order, OrderRecyclerViewAdapter.RecycleViewHolder> {
    private EditItemListener editItemListener;
    private SelectItemListener selectItemListener;

    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public interface EditItemListener {
        void edit(Order item);
    }

    public interface SelectItemListener {
        void selectItem(Order item);
    }


    public void setEditItemListener(EditItemListener editItemListener) {
        this.editItemListener = editItemListener;
    }

    public void setSelectItemListener(SelectItemListener selectItemListener) {
        this.selectItemListener = selectItemListener;
    }

    public class RecycleViewHolder extends AbstractRecyclerViewHolder<Order> {
        private final TextView clientName;
        private final TextView deadline;
        private final TextView createDate;
        private final TextView toPay;

        private final TextView status;


        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            clientName = itemView.findViewById(R.id.orderItemClientName);
            deadline = itemView.findViewById(R.id.orderItemDeadline);
            createDate = itemView.findViewById(R.id.orderItemDateCreate);
            toPay = itemView.findViewById(R.id.orderItemToPay);
            status = itemView.findViewById(R.id.ordertemStatus);
            LinearLayout layout = itemView.findViewById(R.id.orderItemLayout);
            ImageButton edit = itemView.findViewById(R.id.orderItemEdit);
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
        public void bind(Order item) {
            super.bind(item);
            clientName.setText(getItem().getClient().getName());
            deadline.setText(dateTimeFormatter.format(getItem().getDeadline()));
            createDate.setText(dateTimeFormatter.format(getItem().getCreateDate()));
            double sumOrder = getItem().getOrderLines().stream()
                    .mapToDouble(m -> m.getPrice() * m.getCount()).sum();
            toPay.setText(MoneyUtils.getInstance()
                    .moneyWithCurrencyToString(sumOrder - Optional.ofNullable(getItem().getPrePaymentSum()).orElse(0d)));
            status.setText(getOrderStatus().getText());

        }

        private OrderStatus getOrderStatus() {
            Order item = getItem();
            if (Boolean.TRUE.equals(item.getShipped())) {
                return OrderStatus.SHIPPED;
            }
            if (item.getOrderLines().stream().allMatch(orderLine -> Boolean.TRUE.equals(orderLine.getDone()))) {
                return OrderStatus.DONE;
            }
            return OrderStatus.IN_PROGRESS;
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