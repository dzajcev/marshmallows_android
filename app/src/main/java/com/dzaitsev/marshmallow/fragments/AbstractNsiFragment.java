package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.AbstractRecyclerViewAdapter;
import com.dzaitsev.marshmallow.adapters.AbstractRecyclerViewHolder;
import com.dzaitsev.marshmallow.adapters.listeners.EditItemListener;
import com.dzaitsev.marshmallow.adapters.listeners.OnCreateListener;
import com.dzaitsev.marshmallow.adapters.listeners.SelectItemListener;
import com.dzaitsev.marshmallow.databinding.FragmentAbstractNsiBinding;
import com.dzaitsev.marshmallow.dto.NsiItem;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.NsiResponse;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import retrofit2.Call;

public abstract class AbstractNsiFragment<T extends NsiItem, K extends NsiResponse<T>,
        A extends AbstractRecyclerViewAdapter<T, AbstractRecyclerViewHolder<T>>>
        extends Fragment implements IdentityFragment {


    private FragmentAbstractNsiBinding binding;

    private A mAdapter;

    private Order order;

    private SelectItemListener<T> selectListener;
    private EditItemListener<T> editItemListener;

    private OnCreateListener onCreateListener;

    public void setSelectListener(SelectItemListener<T> selectListener) {
        this.selectListener = selectListener;
    }

    public void setOnCreateListener(OnCreateListener onCreateListener) {
        this.onCreateListener = onCreateListener;
    }

    public void setEditItemListener(EditItemListener<T> editItemListener) {
        this.editItemListener = editItemListener;
    }

    public void setAdapter(A mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAbstractNsiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    protected Boolean determineRequestValue() {
        if (order == null) {
            return Optional.of(binding.checkBoxTriStates.getState()).map(m -> {
                if (m == 1) {
                    binding.checkBoxTriStates.setText("Активные");
                    return true;
                } else if (m == 0) {
                    binding.checkBoxTriStates.setText("Не активные");
                    return false;
                } else {
                    binding.checkBoxTriStates.setText("Все");
                    return null;
                }
            }).orElse(null);
        } else {
            return true;
        }
    }

    protected abstract Call<K> getCall(Boolean bool);

    private void refresh() {
        new NetworkExecutorHelper<>(requireActivity(),
                getCall(determineRequestValue())).invoke(response -> Optional.ofNullable(response.body())
                .ifPresent(r -> {
                    mAdapter.setItems(Optional.of(r)
                            .map(NsiResponse::getItems)
                            .orElseThrow(() -> new RuntimeException("error of fetching data"))
                            .stream()
                            .sorted(Comparator.comparing(NsiItem::getName)).collect(Collectors.toList()));
                    if (!StringUtils.isEmpty(binding.searchField.getQuery().toString())) {
                        mAdapter.filter(binding.searchField.getQuery().toString());
                    }
                }));
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView recyclerView = view.findViewById(R.id.abstractNsiListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        order = Optional.ofNullable(getArguments())
                .map(m -> GsonHelper.deserialize(m.getString("order"), Order.class)).orElse(null);
        if (order != null) {
            binding.checkBoxTriStates.setVisibility(View.GONE);
        }
        binding.checkBoxTriStates.setOnCheckedChangeListener((buttonView, isChecked) -> refresh());
        refresh();
        if (selectListener == null) {
            binding.abstractNsiListBack.setVisibility(View.GONE);
        }
        if (selectListener != null) {
            mAdapter.setSelectItemListener(item -> {
                if (selectListener != null) {
                    selectListener.selectItem(item);
                }
            });
        }
        if (editItemListener != null) {
            mAdapter.setEditItemListener(item -> {
                if (editItemListener != null) {
                    editItemListener.edit(item);
                }
            });
        }
        binding.abstractNsiListBack.setOnClickListener(v -> Navigation.getNavigation().back());
        mAdapter.setFilterPredicate(s -> item -> item.getName().toLowerCase().contains(s.toLowerCase()));
        recyclerView.setAdapter(mAdapter);
        binding.searchField.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return false;
            }
        });
        binding.nsiCreate.setOnClickListener(view1 -> {
            if (onCreateListener != null) {
                onCreateListener.onCreateItem();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mAdapter = null;
    }
}