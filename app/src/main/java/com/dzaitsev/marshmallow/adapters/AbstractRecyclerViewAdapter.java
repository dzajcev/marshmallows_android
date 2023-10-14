package com.dzaitsev.marshmallow.adapters;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
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

    @Override
    public void onBindViewHolder(@NonNull A holder, int position) {
        if (position % 2 == 0) {
            holder.getView().setBackgroundColor(ContextCompat.getColor(holder.getView().getContext(), R.color.row_1));
        } else {
            holder.getView().setBackgroundColor(ContextCompat.getColor(holder.getView().getContext(), R.color.row_2));
        }
        holder.bind(showItems.get(position));

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

    @Override
    public int getItemCount() {
        return showItems.size();
    }
}
