package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Client;

public class ClientRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Client, AbstractRecyclerViewHolder<Client>> {

    @Override
    public void onBindViewHolder(@NonNull AbstractRecyclerViewHolder<Client> holder, int position) {
        super.onBindViewHolder(holder, position);
        if (!getShowItems().get(position).isActive()) {
            holder.changeBackgroundTintColor(ContextCompat.getColor(holder.getView().getContext(), R.color.grey));
        }
    }
    private static class RecycleViewHolder extends AbstractRecyclerViewHolder<Client> {
        private final TextView name;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.clientName);
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