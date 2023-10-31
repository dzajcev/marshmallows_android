package com.dzaitsev.marshmallow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.InviteRequest;
import com.dzaitsev.marshmallow.dto.InviteRequestDirection;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class InviteRequestsRecyclerViewAdapter extends AbstractRecyclerViewAdapter<InviteRequest, AbstractRecyclerViewHolder<InviteRequest>> {
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private OnDeleteListener onDeleteListener;
    private OnAcceptListener onAcceptListener;

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public void setOnAcceptListener(OnAcceptListener onAcceptListener) {
        this.onAcceptListener = onAcceptListener;
    }

    public interface OnDeleteListener {
        void onDelete(InviteRequest request);
    }

    public interface OnAcceptListener {
        void onAccept(InviteRequest accept);
    }

    @NonNull
    @Override
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invite_request_list_item, parent, false);
        return new RecycleViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractRecyclerViewHolder<InviteRequest> holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    private class RecycleViewHolder extends AbstractRecyclerViewHolder<InviteRequest> {
        private final TextView name;
        private final TextView createDate;
        private final TextView acceptDate;
        private final ImageButton accept;

        public RecycleViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtInviteRequestUserName);
            createDate = itemView.findViewById(R.id.txtInviteRequestCreateDate);
            acceptDate = itemView.findViewById(R.id.txtInviteRequestAcceptDate);
            itemView.findViewById(R.id.btnDeleteInviteRequest).setOnClickListener(v -> {
                if (onDeleteListener != null) {
                    onDeleteListener.onDelete(getItem());
                }
            });
            accept = itemView.findViewById(R.id.btnAcceptInviteRequest);
            accept.setOnClickListener(v -> {
                if (onAcceptListener != null) {
                    onAcceptListener.onAccept(getItem());
                }
            });

        }

        @Override
        public void bind(InviteRequest item) {
            super.bind(item);
            name.setText(getItem().getUser().getFullName());
            createDate.setText(dateTimeFormatter.format(getItem().getCreateDate()));
            acceptDate.setText(Optional.ofNullable(getItem().getAcceptDate())
                    .map(m -> dateTimeFormatter.format(getItem().getAcceptDate())).orElse(""));

            if (item.getAcceptDate() != null || item.getDirection() == InviteRequestDirection.OUTGOING) {
                accept.setVisibility(View.GONE);
            } else {
                accept.setVisibility(View.VISIBLE);
            }
        }
    }
}

