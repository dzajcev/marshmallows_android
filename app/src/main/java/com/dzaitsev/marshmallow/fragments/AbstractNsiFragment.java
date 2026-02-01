package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
import com.dzaitsev.marshmallow.dto.response.ResultResponse;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Setter;
import retrofit2.Call;

public abstract class AbstractNsiFragment<T extends NsiItem,
        A extends AbstractRecyclerViewAdapter<T, AbstractRecyclerViewHolder<T>>>
        extends Fragment implements IdentityFragment {


    private FragmentAbstractNsiBinding binding;

    private A mAdapter;

    private Order order;

    @Setter
    private SelectItemListener<T> selectListener;
    @Setter
    private EditItemListener<T> editItemListener;

    @Setter
    private OnCreateListener onCreateListener;

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

    protected abstract Call<ResultResponse<List<T>>> getCall(Boolean bool);

    private void refresh() {

        new NetworkExecutorHelper<>(requireActivity(),
                getCall(determineRequestValue())).invoke(response -> Optional.ofNullable(response.body())
                .ifPresent(r -> {
                    if (mAdapter != null) {
                        mAdapter.setItems(Optional.of(r)
                                .map(ResultResponse::getData)
                                .orElseThrow(() -> new RuntimeException("error of fetching data"))
                                .stream()
                                .sorted(Comparator.comparing(NsiItem::getName)).collect(Collectors.toList()));
                        if (!StringUtils.isEmpty(binding.searchField.getText().toString())) {
                            mAdapter.filter(binding.searchField.getText().toString());
                        }
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
        binding.searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mAdapter.filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        binding.nsiCreate.setOnClickListener(view1 -> {
            if (onCreateListener != null) {
                onCreateListener.onCreateItem();
            }
        });

        refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mAdapter = null;
    }
}