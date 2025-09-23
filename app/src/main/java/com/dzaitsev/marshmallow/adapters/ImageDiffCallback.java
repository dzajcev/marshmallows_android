package com.dzaitsev.marshmallow.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.dzaitsev.marshmallow.dto.Attachment;

public class ImageDiffCallback extends DiffUtil.ItemCallback<Attachment> {

    @Override
    public boolean areItemsTheSame(@NonNull Attachment oldItem, @NonNull Attachment newItem) {
        // URL-адреса уникальны, поэтому их можно использовать для проверки идентичности элемента
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull Attachment oldItem, @NonNull Attachment newItem) {
        // Для строк содержимое и элемент - это одно и то же
        return oldItem.equals(newItem);
    }
}