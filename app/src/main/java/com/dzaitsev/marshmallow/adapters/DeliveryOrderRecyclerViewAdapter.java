package com.dzaitsev.marshmallow.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.LinkChannelPicker;
import com.dzaitsev.marshmallow.components.OrderSimpleDialog;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.service.CallPhoneService;
import com.dzaitsev.marshmallow.service.SendSmsService;
import com.dzaitsev.marshmallow.service.SendWhatsappService;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.util.Optional;

public class DeliveryOrderRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Order, DeliveryOrderRecyclerViewAdapter.RecycleViewHolder> {
    private DeleteItemListener deleteItemListener;

    public interface DeleteItemListener {
        void deleteItem(Order item);
    }

    public void setDeleteItemListener(DeleteItemListener deleteItemListener) {
        this.deleteItemListener = deleteItemListener;
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (getShowItems().get(position).getOrderStatus() == OrderStatus.SHIPPED) {
            holder.changeBackgroundTintColor(ContextCompat.getColor(holder.getView().getContext(), R.color.light_green));
        }
    }

    public class RecycleViewHolder extends AbstractRecyclerViewHolder<Order> {
        private final TextView deliveryOrderId;
        private final TextView deliveryOrderClientName;
        private final TextView deliveryOrderAddress;
        private final TextView deliveryOrderPhone;
        private final TextView deliveryOrderSum;
        private final TextView deliveryOrderToPay;
        private final ImageButton deliveryOrderItemDelete;

        @SuppressLint("ResourceAsColor")
        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            deliveryOrderId = itemView.findViewById(R.id.deliveryOrderId);
            deliveryOrderClientName = itemView.findViewById(R.id.deliveryOrderClientName);
            deliveryOrderAddress = itemView.findViewById(R.id.deliveryOrderAddress);
            deliveryOrderPhone = itemView.findViewById(R.id.deliveryOrderPhone);
            deliveryOrderSum = itemView.findViewById(R.id.deliveryOrderSum);
            deliveryOrderToPay = itemView.findViewById(R.id.deliveryOrderToPay);
            deliveryOrderItemDelete = itemView.findViewById(R.id.deliveryOrderItemDelete);
            ImageButton deliveryOrderListGoods = itemView.findViewById(R.id.deliveryOrderListGoods);
            ImageButton deliveryOrderConnect = itemView.findViewById(R.id.deliveryOrderConnect);
            ImageButton deliveryOrderShipped = itemView.findViewById(R.id.deliveryOrderShipped);
            deliveryOrderListGoods.setOnClickListener(v -> OrderSimpleDialog.builder(getView().getContext())
                    .setTitle("Содержимое заказа")
                    .setItems(getItem().getOrderLines())
                    .build()
                    .show());
            deliveryOrderConnect.setOnClickListener(v -> LinkChannelPicker.builder(v.getContext())
                    .setTitle("Выберите канал связи")
                    .setAction((alertDialog, linkChannel) -> {
                        String phone = String.format("+7%s", getItem().getPhone());
                        switch (linkChannel) {
                            case PHONE ->
                                    CallPhoneService.getInstance().call(getView().getContext(), phone);
                            case WHATSAPP ->
                                    SendWhatsappService.getInstance().send(getView().getContext(), phone, "");
                            case SMS ->
                                    SendSmsService.getInstance().sendSms(getView().getContext(), phone, "");
                        }
                        alertDialog.dismiss();
                    }).build()
                    .show());
            deliveryOrderShipped.setOnClickListener(v -> {
                if (getItem().getOrderStatus() == OrderStatus.SHIPPED) {
                    getItem().setOrderStatus(OrderStatus.IN_DELIVERY);
                } else if (getItem().getOrderStatus() == OrderStatus.IN_DELIVERY) {
                    getItem().setOrderStatus(OrderStatus.SHIPPED);
                }
                setShipped(getItem().getOrderStatus());
            });
        }

        @Override
        public void bind(Order item) {
            super.bind(item);
            deliveryOrderId.setText(String.format("#%s", getItem().getId()));
            deliveryOrderClientName.setText(getItem().getClient().getName());
            deliveryOrderAddress.setText(getItem().getDeliveryAddress());
            deliveryOrderPhone.setText(getItem().getPhone().replaceFirst("(\\d{3})(\\d{3})(\\d{2})(\\d+)", "+7($1)-$2-$3-$4"));
            deliveryOrderSum.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(calcToPay(getItem())));
            deliveryOrderToPay.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(calcToPay(getItem())));
            deliveryOrderItemDelete.setOnClickListener(v -> {
                if (deleteItemListener != null && getItem().getOrderStatus() != OrderStatus.SHIPPED) {
                    deleteItemListener.deleteItem(getItem());
                }
            });
            if (getItem().getOrderStatus() == OrderStatus.SHIPPED || deleteItemListener == null) {
                deliveryOrderItemDelete.setVisibility(View.GONE);
            } else {
                deliveryOrderItemDelete.setVisibility(View.VISIBLE);
            }
        }

        @SuppressLint("ResourceAsColor")
        public void setShipped(OrderStatus status) {
            if (status == OrderStatus.SHIPPED) {
                deliveryOrderItemDelete.setVisibility(View.GONE);
                changeBackgroundTintColor(ContextCompat.getColor(getView().getContext(), R.color.light_green));
            } else {
                deliveryOrderItemDelete.setVisibility(View.VISIBLE);
                changeBackgroundTintColor();
            }
        }
    }

    public void setShipped(boolean shipped, Order order) {

        order.setOrderStatus(shipped ? OrderStatus.SHIPPED : OrderStatus.IN_DELIVERY);
        notifyDataSetChanged();
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

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.delivery_order_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }


}