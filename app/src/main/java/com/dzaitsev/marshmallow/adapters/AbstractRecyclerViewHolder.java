package com.dzaitsev.marshmallow.adapters;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.listeners.EditItemListener;
import com.dzaitsev.marshmallow.adapters.listeners.SelectItemListener;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public abstract class AbstractRecyclerViewHolder<T> extends RecyclerView.ViewHolder {
    @Getter
    private T item;

    @Getter
    private final View view;

    protected int originalColor = R.color.white;
    private SelectItemListener<T> selectItemListener;
    private EditItemListener<T> editItemListener;

    public AbstractRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        this.view = itemView;
    }

    public void setEditItemListener(EditItemListener<T> editItemListener) {
        this.editItemListener = editItemListener;
        if (selectItemListener != null) {
            getView().setOnLongClickListener(v -> {
                if (editItemListener != null) {
                    editItemListener.edit(getItem());
                }
                return false;
            });
        } else {
            if (this.editItemListener != null) {
                getView().setOnClickListener(v -> {
                    if (this.editItemListener != null) {
                        editItemListener.edit(getItem());
                    }
                });
            }
        }
    }

    public void setSelectItemListener(SelectItemListener<T> selectItemListener) {
        this.selectItemListener = selectItemListener;
        if (selectItemListener != null) {
            itemView.setOnClickListener(view -> {
                if (this.selectItemListener != null) {
                    selectItemListener.selectItem(getItem());
                }
            });
        }
    }

    public void changeBackgroundTintColor(int color) {
        getViewsForChangeColor().forEach(v -> v.setBackgroundColor(color));
    }

    protected List<View> getViewsForChangeColor() {
        return getViewsRecursive(getView());
    }

    private List<View> getViewsRecursive(View parent) {
        List<View> result = new ArrayList<>();
        getViewsRecursiveInternal(parent, result);
        return result;
    }

    private void getViewsRecursiveInternal(View parent, List<View> result) {
        result.add(parent);
        if (parent instanceof ViewGroup viewGroup) {
            for (int index = 0; index < viewGroup.getChildCount(); index++) {
                View nextChild = viewGroup.getChildAt(index);
                getViewsRecursiveInternal(nextChild, result);
            }
        }
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

    public void bind(T item) {
        this.item = item;
        changeBackgroundTintColor(getBackgroundColor());
    }


}
