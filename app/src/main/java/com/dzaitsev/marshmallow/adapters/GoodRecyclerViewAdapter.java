package com.dzaitsev.marshmallow.adapters;

import android.annotation.SuppressLint;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        private  ImageButton edit;

        @SuppressLint("ClickableViewAccessibility")
        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.goodListName);
            price = itemView.findViewById(R.id.goodListPrice);
            View layout = itemView.findViewById(R.id.goodItemLayout);
            edit = itemView.findViewById(R.id.goodItemEdit);
            edit.setOnClickListener(v -> {
                if (editItemListener != null) {
                    editItemListener.edit(getItem());
                }
            });

            if (selectItemListener != null) {
                edit.setVisibility(View.GONE);
                layout.setOnClickListener(v -> selectItemListener.selectItem(getItem()));
            }
        }
        protected List<View> getViewsForChangeColor() {
            return Stream.of(getView(), edit).collect(Collectors.toList());
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
