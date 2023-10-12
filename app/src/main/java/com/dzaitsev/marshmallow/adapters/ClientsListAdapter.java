package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientsListAdapter extends RecyclerView.Adapter<ClientsListAdapter.ClientListViewHolder> {
    private List<Client> originalList = new ArrayList<>();

    private List<Client> showList = new ArrayList<>();
    private View view;

    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Client item);
    }

    public ClientsListAdapter(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class ClientListViewHolder extends RecyclerView.ViewHolder {
        private Client client;
        private final TextView name;

        public ClientListViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.client_list_name);
            name.setOnClickListener(v -> onItemClickListener.onItemClick(client));
        }

        public void bind(Client client) {
            name.setText(client.getName());
            this.client = client;
        }
    }

    public void filter(String namePart) {
        showList.clear();
        if (namePart != null && !namePart.isEmpty()) {
            showList.addAll(originalList.stream()
                    .filter(f -> f.getName().toLowerCase().contains(namePart.toLowerCase()))
                    .collect(Collectors.toList()));
        } else {
            showList.addAll(originalList);
        }
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public ClientListViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_list_item, parent, false);
        return new ClientListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientListViewHolder holder, int position) {
        if (position % 2 == 0) {
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.grey));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.grey_1));
        }
        holder.bind(showList.get(position));

    }

    public void setItems(List<Client> items) {
        originalList = items;
        showList = new ArrayList<>(originalList);
        notifyDataSetChanged();
    }

    public void clearItems() {
        originalList.clear();
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return showList.size();
    }

}