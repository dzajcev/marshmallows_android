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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.adapters.GoodRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentGoodsBinding;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class GoodsFragment extends Fragment {

    private FragmentGoodsBinding binding;
    private GoodRecyclerViewAdapter mAdapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentGoodsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Зефирки и прочее");
        binding.newGood.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("good", new Good());
            Navigation.getNavigation(requireActivity()).goForward(new GoodCardFragment(), bundle);
        });
        mAdapter = new GoodRecyclerViewAdapter();
        new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().getGoods(),
                response -> Optional.ofNullable(response.body())
                        .ifPresent(goodsResponse -> {
                            mAdapter.setItems(Optional.of(goodsResponse)
                                    .orElse(new GoodsResponse()).getGoods().stream()
                                    .sorted(Comparator.comparing(Good::getName)).collect(Collectors.toList()));
                            if (!StringUtils.isEmpty(binding.searchGood.getQuery().toString())) {
                                mAdapter.filter(binding.searchGood.getQuery().toString());
                            }
                        })).invoke();
        binding.goodsList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mAdapter.setEditItemListener(good -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("good", good);
            Navigation.getNavigation(requireActivity()).goForward(new GoodCardFragment(), bundle);
        });
        mAdapter.setFilterPredicate(s -> good -> good.getName().toLowerCase().contains(s.toLowerCase()));
        binding.goodsList.setAdapter(mAdapter);
        Optional.ofNullable(getArguments())
                .ifPresent(bundle -> {
                    Integer orderline = bundle.getSerializable("orderline", Integer.class);
                    Order order = bundle.getSerializable("order", Order.class);
                    String source = bundle.getString("source");
                    if (orderline != null && order != null) {
                        binding.newGood.setVisibility(View.GONE);
                        mAdapter.setSelectItemListener(item -> {
                            Bundle newBundle = new Bundle();
                            order.getOrderLines().stream()
                                    .filter(f -> f.getGood() != null)
                                    .filter(f -> f.getGood().getId().equals(item.getId()))
                                    .findAny().ifPresentOrElse(orderLine -> {
                                        orderLine.setCount(orderLine.getCount() + 1);
                                        order.getOrderLines().removeIf(f -> f.getNum().equals(orderline));
                                    }, () -> order.getOrderLines().stream()
                                            .filter(f -> f.getNum().equals(orderline))
                                            .findAny()
                                            .ifPresent(orderLine -> {
                                                orderLine.setGood(item);
                                                orderLine.setPrice(item.getPrice());
                                                orderLine.setCount(1);
                                                newBundle.putSerializable("order", order);
                                            }));
                            newBundle.putSerializable("order", order);
                            Navigation.getNavigation(requireActivity()).back();
                        });
                    }
                });
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