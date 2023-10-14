package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GoodRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Good, GoodRecyclerViewAdapter.RecycleViewHolder> {

    public interface EditItemListener {
        void edit(Good item);
    }

    private final List<EditItemListener> editItemListeners = new CopyOnWriteArrayList<>();

    public void addEditItemListener(EditItemListener editItemListener) {
        this.editItemListeners.add(editItemListener);
    }

    public void removeEditItemListener(EditItemListener editItemListener) {
        this.editItemListeners.remove(editItemListener);
    }

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.goods_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }

    public class RecycleViewHolder extends AbstractRecyclerViewHolder<Good> {
        private final TextView name;
        private final TextView price;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.goodsListName);
            price = itemView.findViewById(R.id.goodListPrice);
            ImageButton edit = itemView.findViewById(R.id.goodItemEdit);
            edit.setOnClickListener(v -> {
                if (!GoodRecyclerViewAdapter.this.editItemListeners.isEmpty()) {
                    GoodRecyclerViewAdapter.this.editItemListeners
                            .forEach(editItemListener -> editItemListener.edit(getItem()));
                }
            });
        }

        @Override
        public void bind(Good item) {
            super.bind(item);
            name.setText(getItem().getName());
            if (getItem().getPrice() - getItem().getPrice().intValue() == 0) {
                price.setText(String.format("%sр", getItem().getPrice().intValue()));
            } else {
                price.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(getItem().getPrice()));
            }
        }
    }
}
