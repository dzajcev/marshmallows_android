package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

public class OrderLineSimpleRecyclerViewAdapter extends AbstractRecyclerViewAdapter<OrderLine, OrderLineSimpleRecyclerViewAdapter.RecycleViewHolder> {

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_line_simple, parent, false);
        return new RecycleViewHolder(inflate);
    }

    public static class RecycleViewHolder extends AbstractRecyclerViewHolder<OrderLine> {
        private final TextView orderLinesSimpleGoodName;
        private final TextView orderLinesSimplePrice;
        private final TextView orderLinesSimpleCount;
        private final TextView orderLinesSimpleSum;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            orderLinesSimpleGoodName = itemView.findViewById(R.id.orderLinesSimpleGoodName);
            orderLinesSimplePrice = itemView.findViewById(R.id.orderLinesSimplePrice);
            orderLinesSimpleCount = itemView.findViewById(R.id.orderLinesSimpleCount);
            orderLinesSimpleSum = itemView.findViewById(R.id.orderLinesSimpleSum);
        }

        @Override
        public void bind(OrderLine item) {
            super.bind(item);
            orderLinesSimpleGoodName.setText(item.getGood().getName());
            orderLinesSimplePrice.setText(MoneyUtils.moneyWithCurrencyToString(getItem().getPrice()));
            orderLinesSimpleCount.setText(String.format("%s", item.getCount()));
            orderLinesSimpleSum.setText(MoneyUtils.moneyWithCurrencyToString(getItem().getPrice() * getItem().getCount()));
        }
    }
}
