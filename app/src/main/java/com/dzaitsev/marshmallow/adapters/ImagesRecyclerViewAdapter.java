package com.dzaitsev.marshmallow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.Attachment;

// Предположим, что вы передаете список строк (URL-адресов изображений)
// Если у вас другой тип данных для изображений (например, объект Good.Image),
// вам нужно будет адаптировать конструктор и метод onBindViewHolder
public class ImagesRecyclerViewAdapter extends ListAdapter<Attachment, ImagesRecyclerViewAdapter.ImageViewHolder> {

    private final Context context; // Контекст все еще может быть полезен для Glide или LayoutInflater
    private final int size;
    private final OnImagePickListener listener;

    // Конструктор теперь принимает DiffUtil.ItemCallback
    public ImagesRecyclerViewAdapter(@NonNull Context context, int size, OnImagePickListener listener) {
        super(new ImageDiffCallback());
        this.context = context;
        this.size = size;
        this.listener = listener;
    }

    public interface OnImagePickListener {
        void onPickImage(int position);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Attachment image = getItem(position);
        ViewGroup.LayoutParams params = holder.imageViewItem.getLayoutParams();

        params.width = size; // например, 100 px или из ресурса
        params.height = size;
        holder.imageViewItem.setLayoutParams(params);
        if (image == null) {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.upload)
//                    .placeholder(R.drawable.ic_placeholder_image)
//                    .error(R.drawable.ic_error_image)
                    .into(holder.imageViewItem);
            holder.imageViewItem.setOnClickListener(v -> listener.onPickImage(holder.getAdapterPosition()));

        } else {
            String url;
            if (image.getThumbnailUrl() != null) {
                url = image.getThumbnailUrl();
            } else if (image.getUrl() != null) {
                url = image.getUrl();
            } else {
                url = "";
            }
            Glide.with(holder.itemView.getContext())
                    .load(url)
//                    .placeholder(R.drawable.ic_placeholder_image)
//                    .error(R.drawable.ic_error_image)
                    .into(holder.imageViewItem);
        }

    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewItem;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
        }
    }
}