package com.dzaitsev.marshmallow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Attachment;
import com.dzaitsev.marshmallow.fragments.FullScreenImageDialogFragment;

import lombok.Getter;

public class ImagesRecyclerViewAdapter extends ListAdapter<Attachment, RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 0;

    private final OnImagePickListener listener;
    private final Context context;

    public interface OnImagePickListener {
        void onPickImage(int position);

        void onDeleteImage(int position);  // новый метод для удаления

        void onSetPrimary(int position);   // новый метод для шаринга
    }

    public ImagesRecyclerViewAdapter(Context context, OnImagePickListener listener) {
        super(new ImageDiffCallback());
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UploadViewHolder) {
            holder.itemView.setOnClickListener(v -> listener.onPickImage(holder.getAdapterPosition()));
        } else if (holder instanceof ImageViewHolder iv) {
            Attachment image = getItem(position);
            String url = (image.getThumbnailUrl() != null) ? image.getThumbnailUrl()
                    : (image.getUrl() != null ? image.getUrl() : "");
            iv.setPrimary(image.isPrimary());
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .centerCrop()
                    .error(R.drawable.error)
                    .into(iv.imageViewItem);
            holder.itemView.setOnClickListener(v -> {
                Attachment a = getItem(holder.getBindingAdapterPosition());
                if (a != null && a.getUrl() != null) {
                    FullScreenImageDialogFragment dialog = FullScreenImageDialogFragment
                            .newInstance(image.getUrl());
                    dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "full_screen_image");
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.image_context_menu, popup.getMenu());
                popup.getMenu().findItem(R.id.menu_primary).setVisible(!image.isPrimary());
                popup.setOnMenuItemClickListener(item -> {
                    int pos = holder.getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return false;
                    int id = item.getItemId();
                    if (id == R.id.menu_delete) {
                        listener.onDeleteImage(pos);
                        return true;
                    } else if (id == R.id.menu_primary) {
                        listener.onSetPrimary(pos);
                        return true;
                    } else {
                        return false;
                    }
                });
                popup.show();
                return true;
            });
        }
    }

    @Getter
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewItem;
        ImageView star;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            star = itemView.findViewById(R.id.primary_image);
        }

        public void setPrimary(boolean primary) {
            if (primary) {
                star.setVisibility(View.VISIBLE);
            } else {
                star.setVisibility(View.GONE);
            }
        }
    }

    public static class UploadViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewItem;

        UploadViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
        }
    }
}