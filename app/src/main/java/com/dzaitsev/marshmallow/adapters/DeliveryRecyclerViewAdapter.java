package com.dzaitsev.marshmallow.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.DeliveryStatus;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;
import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryRecyclerViewAdapter extends AbstractRecyclerViewAdapter<Delivery, DeliveryRecyclerViewAdapter.RecycleViewHolder> {
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void onBindViewHolder(@NonNull RecycleViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Delivery item = getShowItems().get(position);

        // Работаем с цветом фона карточки через MaterialCardView
        if (holder.itemView instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) holder.itemView;

            if (item.getDeliveryStatus() == DeliveryStatus.DONE) {
                card.setCardBackgroundColor(ContextCompat.getColor(holder.getView().getContext(), R.color.green));
            } else if (item.getDeliveryStatus() == DeliveryStatus.IN_PROGRESS) {
                card.setCardBackgroundColor(ContextCompat.getColor(holder.getView().getContext(), R.color.light_green));
            } else {
                // Сбрасываем на белый для остальных статусов (важно при переиспользовании view)
                card.setCardBackgroundColor(Color.WHITE);
            }
        }
    }

    public static class RecycleViewHolder extends AbstractRecyclerViewHolder<Delivery> {

        private final TextView id;
        private final TextView deliveryDate;
        private final TextView start;
        private final TextView end;
        private final TextView deliveryStatus;

        // Новые поля для прогресса
        private final TextView ordersCountText;
        private final ProgressBar ordersProgress;

        private final View executorLayout;
        private final TextView txtAuthor;
        private final TextView txtExecutor;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.deliveryItemId);
            deliveryDate = itemView.findViewById(R.id.deliveryDate);
            start = itemView.findViewById(R.id.deliveryStart);
            end = itemView.findViewById(R.id.deliveryEnd);
            deliveryStatus = itemView.findViewById(R.id.deliveryStatus);

            // Инициализация новых View
            ordersCountText = itemView.findViewById(R.id.ordersCountText);
            ordersProgress = itemView.findViewById(R.id.ordersProgress);

            executorLayout = itemView.findViewById(R.id.executorLayout);
            txtAuthor = itemView.findViewById(R.id.txtAuthor);
            txtExecutor = itemView.findViewById(R.id.txtExecutor);
        }

        @Override
        public void bind(Delivery item) {
            super.bind(item);
            id.setText(String.format("#%s", getItem().getId()));

            if (item.getDeliveryDate() != null) {
                deliveryDate.setText(dateTimeFormatter.format(item.getDeliveryDate()));
            }
            if (item.getStart() != null) {
                start.setText(timeFormatter.format(item.getStart()));
            }
            if (item.getEnd() != null) {
                end.setText(timeFormatter.format(item.getEnd()));
            }

            deliveryStatus.setText(item.getDeliveryStatus().getText());

            // --- Логика подсчета заказов и прогресса ---
            int total = Optional.ofNullable(item.getOrders()).map(List::size).orElse(0);
            long doneCount = Optional.ofNullable(item.getOrders()).orElse(new ArrayList<>())
                    .stream().filter(f -> f.getOrderStatus() == OrderStatus.SHIPPED)
                    .count();
            int done = (int) doneCount;

            // Установка текста "5 / 10"
            ordersCountText.setText(String.format("%d / %d", done, total));

            // Установка прогресс-бара
            if (total > 0) {
                int progress = (int) ((float) done / total * 100);
                ordersProgress.setProgress(progress);
            } else {
                ordersProgress.setProgress(0);
            }
            // -------------------------------------------

            if (item.getCreateUser() != null && item.getExecutor() != null && !item.getCreateUser().equals(item.getExecutor())) {
                executorLayout.setVisibility(View.VISIBLE);
                AuthorizationHelper.getInstance().getUserData()
                        .ifPresent(user -> {
                            if (item.getCreateUser().getId().equals(user.getId())) {
                                txtAuthor.setText("Я");
                            } else {
                                txtAuthor.setText(item.getCreateUser().getFullName());
                            }
                            if (item.getExecutor().getId().equals(user.getId())) {
                                txtExecutor.setText("Я");
                            } else {
                                txtExecutor.setText(item.getExecutor().getFullName());
                            }
                        });

            } else {
                executorLayout.setVisibility(View.GONE);
            }
        }
    }

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.delivery_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }
}
