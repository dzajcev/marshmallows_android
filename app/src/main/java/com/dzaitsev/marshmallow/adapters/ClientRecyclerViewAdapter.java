package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Client;

public class ClientRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Client, ClientRecyclerViewAdapter.RecycleViewHolder> {
    private EditItemListener editItemListener;
    private SelectItemListener selectItemListener;

    public interface EditItemListener {
        void edit(Client item);
    }

    public interface SelectItemListener {
        void selectItem(Client item);
    }


    public void setEditItemListener(ClientRecyclerViewAdapter.EditItemListener editItemListener) {
        this.editItemListener = editItemListener;
    }

    public void setSelectItemListener(ClientRecyclerViewAdapter.SelectItemListener selectItemListener) {
        this.selectItemListener = selectItemListener;
    }

    public class RecycleViewHolder extends AbstractRecyclerViewHolder<Client> {
        private final TextView name;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.orderItemClientName);
            LinearLayout layout = itemView.findViewById(R.id.clientItemLayout);
            ImageButton edit = itemView.findViewById(R.id.clientItemEdit);
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
        public void bind(Client item) {
            super.bind(item);
            name.setText(getItem().getName());
        }
    }

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }


}