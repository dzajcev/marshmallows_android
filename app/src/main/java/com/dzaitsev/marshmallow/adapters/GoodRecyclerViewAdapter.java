package com.dzaitsev.marshmallow.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

public class GoodRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Good, AbstractRecyclerViewHolder<Good>> {
    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.goods_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractRecyclerViewHolder<Good> holder, int position) {
        super.onBindViewHolder(holder, position);
        if (!getShowItems().get(position).isActive()) {
            holder.changeBackgroundTintColor(ContextCompat.getColor(holder.getView().getContext(), R.color.grey));
        }
    }

    private static class RecycleViewHolder extends AbstractRecyclerViewHolder<Good> {
        private final TextView name;
        private final TextView price;

        @SuppressLint("ClickableViewAccessibility")
        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.goodListName);
            price = itemView.findViewById(R.id.goodListPrice);
        }

        @Override
        public void bind(Good item) {
            super.bind(item);
            name.setText(getItem().getName());
            price.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(getItem().getPrice()));
        }
    }
}
