package com.dzaitsev.marshmallow.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.OrderRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentOrdersBinding;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;
import com.dzaitsev.marshmallow.dto.response.OrderResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;

    private OrderRecyclerViewAdapter mAdapter;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().getOrders(),
                response -> Optional.ofNullable(response.body())
                        .ifPresent(orderResponse -> {
                            mAdapter.setItems(Optional.of(orderResponse)
                                    .orElse(new OrderResponse()).getOrders().stream()
                                    .sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate())).collect(Collectors.toList()));
//                            if (!StringUtils.isEmpty(binding.searchGood.getQuery().toString())) {
//                                mAdapter.filter(binding.searchGood.getQuery().toString());
//                            }
                        })).invoke();
        binding.ordersList.setLayoutManager(new LinearLayoutManager(view.getContext()));

//        mAdapter.setEditItemListener(good -> {
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("good", good);
//            NavHostFragment.findNavController(OrdersFragment.this)
//                    .navigate(R.id.action_goodsFragment_to_goodCard, bundle);
//        });
        mAdapter = new OrderRecyclerViewAdapter();
        mAdapter.setFilterPredicate(s -> order -> order.getClient().getName().toLowerCase().contains(s.toLowerCase()));
        binding.ordersList.setAdapter(mAdapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}