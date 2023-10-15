package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

public class GoodRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Good, GoodRecyclerViewAdapter.RecycleViewHolder> {

    private EditItemListener editItemListener;
    private SelectItemListener selectItemListener;

    public interface EditItemListener {
        void edit(Good item);
    }

    public interface SelectItemListener {
        void selectItem(Good item);
    }


    public void setEditItemListener(EditItemListener editItemListener) {
        this.editItemListener = editItemListener;
    }

    public void setSelectItemListener(SelectItemListener selectItemListener) {
        this.selectItemListener = selectItemListener;
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
            name = itemView.findViewById(R.id.goodListName);
            price = itemView.findViewById(R.id.goodListPrice);
            LinearLayout layout = itemView.findViewById(R.id.goodItemLayout);
            ImageButton edit = itemView.findViewById(R.id.goodItemEdit);
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
