package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Price;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.time.format.DateTimeFormatter;

public class PriceHistoryRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Price, PriceHistoryRecyclerViewAdapter.RecycleViewHolder> {

    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.price_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }

    public static class RecycleViewHolder extends AbstractRecyclerViewHolder<Price> {
        private final TextView createDate;
        private final TextView price;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            createDate = itemView.findViewById(R.id.priceListDateCreate);
            price = itemView.findViewById(R.id.priceListPrice);
        }

        @Override
        public void bind(Price item) {
            super.bind(item);
            createDate.setText(dateTimeFormatter.format(getItem().getCreateDate()));
            price.setText(MoneyUtils.moneyWithCurrencyToString(getItem().getPrice()));
        }
    }
}
