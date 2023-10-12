package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Good;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GoodsListAdapter extends RecyclerView.Adapter<GoodsListAdapter.GoodsListViewHolder> {
    private final List<Good> goods = new ArrayList<>();

    protected SelectGoodItemListener selectGoodItemListener;

    private View view;

    public class GoodsListViewHolder extends RecyclerView.ViewHolder {
        private Good good;
        private final TextView name;
        private final TextView price;

        private final NumberFormat formatter = new DecimalFormat("#0.00");

        public GoodsListViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.client_list_name);
            price = itemView.findViewById(R.id.good_list_price);
            ImageButton edit = itemView.findViewById(R.id.client_list_edit);
            edit.setOnClickListener(v -> {
                if (selectGoodItemListener != null) {
                    selectGoodItemListener.selectItem(good);
                }
            });
        }

        public void bind(Good good) {
            name.setText(good.getName());

            price.setText(formatter.format(good.getPrice()));
            this.good = good;
        }
    }

    public interface SelectGoodItemListener {
        void selectItem(Good uuid);
    }

    @NonNull
    @Override
    public GoodsListViewHolder onCreateViewHolder(ViewGroup parent,
                                                  int viewType) {

        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.goods_list_item, parent, false);
        return new GoodsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoodsListViewHolder holder, int position) {
        if (position % 2 == 0) {
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.grey));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.grey_1));
        }
        holder.bind(goods.get(position));

    }

    public void setItems(Collection<Good> items) {
        goods.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return goods.size();
    }

    public void setSelectGoodItemListener(SelectGoodItemListener selectGoodItemListener) {
        this.selectGoodItemListener = selectGoodItemListener;
    }
}