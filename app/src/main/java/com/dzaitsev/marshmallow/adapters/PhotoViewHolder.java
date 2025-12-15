package com.dzaitsev.marshmallow.adapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.google.android.material.button.MaterialButton;

public class PhotoViewHolder extends RecyclerView.ViewHolder {

    public final MaterialButton btnAddPhoto;
    public final RecyclerView recyclerPhotos;

    public PhotoViewHolder(@NonNull View itemView) {
        super(itemView);
        
        btnAddPhoto = itemView.findViewById(R.id.btnAddPhoto);
        recyclerPhotos = itemView.findViewById(R.id.images);
    }
}
