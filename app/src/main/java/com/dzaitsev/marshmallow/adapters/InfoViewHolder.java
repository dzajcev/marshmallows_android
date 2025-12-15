package com.dzaitsev.marshmallow.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InfoViewHolder extends RecyclerView.ViewHolder {

    public final TextInputLayout layoutName;
    public final TextInputEditText etName;
    
    public final TextInputLayout layoutPrice;
    public final TextInputEditText etPrice;
    
    public final TextInputLayout layoutDesc;
    public final TextInputEditText etDesc;
    
    public final TextView tvHistoryHeader;
    public final RecyclerView recyclerHistory;

    public final ImageView mainPhoto;

    public InfoViewHolder(@NonNull View itemView) {
        super(itemView);
        
        // Инициализация полей ввода
        layoutName = itemView.findViewById(R.id.layoutName);
        etName = itemView.findViewById(R.id.goodCardName);
        
        layoutPrice = itemView.findViewById(R.id.layoutPrice);
        etPrice = itemView.findViewById(R.id.goodCardPrice);
        
        layoutDesc = itemView.findViewById(R.id.layoutDesc);
        etDesc = itemView.findViewById(R.id.goodCardDescription);
        mainPhoto = itemView.findViewById(R.id.mainPhoto);
        // Инициализация истории цен
        tvHistoryHeader = itemView.findViewById(R.id.tx1);
        recyclerHistory = itemView.findViewById(R.id.goodCardPriceHistoryList);
    }
}
