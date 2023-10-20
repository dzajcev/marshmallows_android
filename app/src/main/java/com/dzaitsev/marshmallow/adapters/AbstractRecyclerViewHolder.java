package com.dzaitsev.marshmallow.adapters;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRecyclerViewHolder<T> extends RecyclerView.ViewHolder {
    private T item;

    private final View view;

    protected int originalColor = R.color.white;

    public AbstractRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        this.view = itemView;
    }

    public void changeBackgroundTintColor(int color) {
        ColorStateList colorStateList = ColorStateList.valueOf(color);
        getViewsForChangeColor().forEach(v -> v.setBackgroundTintList(colorStateList));
    }

    protected List<View> getViewsForChangeColor() {
        return new ArrayList<>();
    }

    public void changeBackgroundTintColor() {
        changeBackgroundTintColor(originalColor);
    }

    private int getBackgroundColor() {
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable colorDrawable) {
            return colorDrawable.getColor();
        } else {
            return ContextCompat.getColor(view.getContext(), R.color.white);
        }
    }

    public View getView() {
        return view;
    }

    public void bind(T item) {
        this.item = item;
        changeBackgroundTintColor(getBackgroundColor());
    }


    public T getItem() {
        return item;
    }
}
