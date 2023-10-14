package com.dzaitsev.marshmallow.adapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbstractRecyclerViewHolder<T> extends RecyclerView.ViewHolder {
    private T item;

    private View view;

    public AbstractRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        this.view = itemView;
    }

    public View getView() {
        return view;
    }

    public void bind(T item) {
        this.item = item;
    }

    public T getItem() {
        return item;
    }
}
