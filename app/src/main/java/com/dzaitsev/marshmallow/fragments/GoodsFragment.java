package com.dzaitsev.marshmallow.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.GoodRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentGoodsBinding;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutorCallback;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GoodsFragment extends Fragment {

    private FragmentGoodsBinding binding;
    private GoodRecyclerViewAdapter mAdapter;

    private final GoodRecyclerViewAdapter.EditItemListener editItemListener = good -> {
        Bundle bundle = new Bundle();
        bundle.putSerializable("good", good);
        NavHostFragment.findNavController(GoodsFragment.this)
                .navigate(R.id.action_goodsFragment_to_goodCard, bundle);
    };

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentGoodsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView goodsList = view.findViewById(R.id.goodsList);

        binding.newGood.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("good", new Good());
            NavHostFragment.findNavController(GoodsFragment.this)
                    .navigate(R.id.action_goodsFragment_to_goodCard, bundle);
        });
        NetworkService.getInstance().getMarshmallowApi().getGoods().enqueue(new NetworkExecutorCallback<>(requireActivity(),
                response -> Optional.ofNullable(response.body())
                        .ifPresent(goodsResponse -> {
                            mAdapter.setItems(Optional.of(goodsResponse)
                                    .orElse(new GoodsResponse()).getGoods().stream()
                                    .sorted(Comparator.comparing(Good::getName)).collect(Collectors.toList()));
                            if (!StringUtils.isEmpty(binding.searchGood.getQuery().toString())) {
                                mAdapter.filter(binding.searchGood.getQuery().toString());
                            }
                        })));

        goodsList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new GoodRecyclerViewAdapter();
        mAdapter.setEditItemListener(editItemListener);
        Optional.ofNullable(getArguments())
                .ifPresent(bundle -> {
                    Integer orderline = getArguments().getSerializable("orderline", Integer.class);
                    Order order = getArguments().getSerializable("order", Order.class);
                    if (orderline != null && order != null) {
                        binding.newGood.setVisibility(View.GONE);
                        mAdapter.setSelectItemListener(item -> {
                            Bundle newBundle = new Bundle();
                            order.getOrderLines().stream()
                                    .filter(f -> f.getNum().equals(orderline))
                                    .findAny()
                                    .ifPresent(orderLine -> {
                                        orderLine.setGood(item);
                                        orderLine.setPrice(item.getPrice());
                                        orderLine.setCount(1);
                                        newBundle.putSerializable("order", order);
                                    });
                            NavHostFragment.findNavController(GoodsFragment.this)
                                    .navigate(R.id.action_goodsFragment_to_orderGoodsFragment, newBundle);
                        });
                    }
                });

        mAdapter.setFilterPredicate(s -> good -> good.getName().toLowerCase().contains(s.toLowerCase()));

        goodsList.setAdapter(mAdapter);
        binding.searchGood.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
        binding = null;
    }
}