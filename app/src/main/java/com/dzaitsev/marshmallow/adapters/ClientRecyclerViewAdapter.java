package com.dzaitsev.marshmallow.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.utils.AvatarUtils;
import com.google.android.material.card.MaterialCardView;

public class ClientRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Client, AbstractRecyclerViewHolder<Client>> {

    @Override
    public void onBindViewHolder(@NonNull AbstractRecyclerViewHolder<Client> holder, int position) {
        super.onBindViewHolder(holder, position);
        // Если клиент неактивен, затемняем всю карточку, иначе - сбрасываем (делаем белой)
        if (!getShowItems().get(position).isActive()) {
            // Фон карточки для неактивных
            ((MaterialCardView) holder.itemView).setCardBackgroundColor(
                    ContextCompat.getColor(holder.getView().getContext(), R.color.grey));
        } else {
            // Фон карточки для активных (белый)
            ((MaterialCardView) holder.itemView).setCardBackgroundColor(Color.WHITE);
        }
    }

    private static class RecycleViewHolder extends AbstractRecyclerViewHolder<Client> {
        private final TextView name;
        private final TextView avatarText;
        private final MaterialCardView avatarContainer;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.clientName);
            avatarText = itemView.findViewById(R.id.avatarText);
            avatarContainer = itemView.findViewById(R.id.avatarContainer);
        }

        @Override
        public void bind(Client item) {
            super.bind(item);
            String clientName = item.getName();

            name.setText(clientName);
            avatarText.setText(AvatarUtils.getInitials(clientName));
            avatarText.setTextColor(Color.DKGRAY);
            avatarContainer.setCardBackgroundColor(AvatarUtils.getColorForName(clientName));
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
