package com.dzaitsev.marshmallow.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.dzaitsev.marshmallow.dto.Attachment;

public class ImageDiffCallback extends DiffUtil.ItemCallback<Attachment> {

    @Override
    public boolean areItemsTheSame(@NonNull Attachment oldItem, @NonNull Attachment newItem) {
        return oldItem.getId().equals(newItem.getId()); // только по ID
    }

    @Override
    public boolean areContentsTheSame(@NonNull Attachment oldItem, @NonNull Attachment newItem) {
        return oldItem.equals(newItem) &&
                oldItem.isPrimary() == newItem.isPrimary(); // учитываем isPrimary
    }
}