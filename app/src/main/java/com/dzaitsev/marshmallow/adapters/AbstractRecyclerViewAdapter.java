package com.dzaitsev.marshmallow.adapters;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.listeners.EditItemListener;
import com.dzaitsev.marshmallow.adapters.listeners.SelectItemListener;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractRecyclerViewAdapter<T, A extends AbstractRecyclerViewHolder<T>> extends RecyclerView.Adapter<A> {
    private List<T> originalItems = new ArrayList<>();
    private List<T> showItems = new ArrayList<>();
    private Function<String, Predicate<T>> filterPredicate;

    private EditItemListener<T> editItemListener;
    private SelectItemListener<T> selectItemListener;


    public void setEditItemListener(EditItemListener<T> editItemListener) {
        this.editItemListener = editItemListener;
    }

    public void setSelectItemListener(SelectItemListener<T> selectItemListener) {
        this.selectItemListener = selectItemListener;
    }

    public EditItemListener<T> getEditItemListener() {
        return editItemListener;
    }

    public SelectItemListener<T> getSelectItemListener() {
        return selectItemListener;
    }

    @Override
    public void onBindViewHolder(@NonNull A holder, int position) {
        int color;
        if (position % 2 == 0) {
            color = ContextCompat.getColor(holder.getView().getContext(), R.color.row_1);
        } else {
            color = ContextCompat.getColor(holder.getView().getContext(), R.color.row_2);
        }
        holder.originalColor = color;
        holder.getView().setBackgroundColor(color);
        holder.bind(showItems.get(position));
        holder.setSelectItemListener(selectItemListener);
        holder.setEditItemListener(editItemListener);
    }

    public void filter(String namePart) {
        Optional.ofNullable(filterPredicate)
                .ifPresent(tPredicate -> {
                    showItems.clear();
                    if (!StringUtils.isEmpty(namePart)) {
                        showItems.addAll(originalItems.stream()
                                .filter(filterPredicate.apply(namePart))
                                .collect(Collectors.toList()));
                    } else {
                        showItems.addAll(originalItems);
                    }
                    notifyDataSetChanged();
                });


    }

    public void setFilterPredicate(Function<String, Predicate<T>> filterPredicate) {
        this.filterPredicate = filterPredicate;
    }

    public void setItems(List<T> items) {
        originalItems = items;
        showItems = new ArrayList<>(originalItems);
        notifyDataSetChanged();
    }

    public List<T> getShowItems() {
        return showItems;
    }

    public List<T> getOriginalItems() {
        return originalItems;
    }

    public void removeItem(int position) {
        T orderLine = getShowItems().get(position);
        getOriginalItems().remove(orderLine);
        showItems = new ArrayList<>(originalItems);
        notifyDataSetChanged();
    }

    public void addItem(T orderLine) {
        originalItems.add(orderLine);
        showItems.add(orderLine);
        notifyItemInserted(showItems.size() - 1);
    }

    @Override
    public int getItemCount() {
        return showItems.size();
    }
}
