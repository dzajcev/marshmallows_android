package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Setter;

public class DeliveryExecutorSelectorRecyclerViewAdapter extends AbstractRecyclerViewAdapter<User, DeliveryExecutorSelectorRecyclerViewAdapter.RecycleViewHolder> {

    private final Map<Integer, Boolean> selected = new HashMap<>();

    private final boolean multiselect;

    @Setter
    private OnSelectListener onSelectListener;

    public DeliveryExecutorSelectorRecyclerViewAdapter(boolean multiselect) {
        this.multiselect = multiselect;
    }

    public interface OnSelectListener {
        void onSelect(User user);
    }

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.delivery_executor_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }

    public void setItems(List<User> items) {
        super.setItems(items);
    }

    public List<User> getSelectedUsers() {
        return getOriginalItems().stream()
                .filter(f -> selected.getOrDefault(f.getId(), false))
                .collect(Collectors.toList());
    }

    public class RecycleViewHolder extends AbstractRecyclerViewHolder<User> {
        private final TextView deliveryExecutorName;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            deliveryExecutorName = itemView.findViewById(R.id.deliveryExecutorName);
        }

        @Override
        public void bind(User item) {
            super.bind(item);
            deliveryExecutorName.setText(item.getFullName());
            CheckBox deliveryExecutorSelect = itemView.findViewById(R.id.deliveryExecutorSelect);
            if (multiselect) {
                getView().setOnClickListener(v -> deliveryExecutorSelect.setChecked(!deliveryExecutorSelect.isChecked()));
                deliveryExecutorSelect.setOnCheckedChangeListener((buttonView, isChecked)
                        -> DeliveryExecutorSelectorRecyclerViewAdapter.this.selected.put(getItem().getId(), isChecked));
                deliveryExecutorSelect.setChecked(Boolean.TRUE.equals(selected.getOrDefault(getItem().getId(), false)));
            } else {
                deliveryExecutorSelect.setVisibility(View.GONE);
                getView().setOnClickListener(v -> {
                    if (onSelectListener != null) {
                        onSelectListener.onSelect(getItem());
                    }
                });
            }
        }
    }
}
