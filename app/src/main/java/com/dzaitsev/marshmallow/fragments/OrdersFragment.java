package com.dzaitsev.marshmallow.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.adapters.OrderRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentOrdersBinding;
import com.dzaitsev.marshmallow.dto.AbstractFilter;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.dto.OrdersFilter;
import com.dzaitsev.marshmallow.dto.response.OrderResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutorWrapper;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrdersFragment extends Fragment implements IdentityFragment {

    public static final String IDENTITY = "ordersFragment";

    private FragmentOrdersBinding binding;

    private OrderRecyclerViewAdapter mAdapter;

    private AbstractFilter<OrderStatus> filter;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String string = preferences.getString("order-filter", "{}");
        if (string.isEmpty()) {
            string = "{}";
        }
        Gson gson = GsonExt.getGson();
        Type type = new TypeToken<OrdersFilter>() {
        }.getType();
        filter = gson.fromJson(string, type);
    }

    private void fillItems() {
        new NetworkExecutorWrapper<>(requireActivity(),
                NetworkService.getInstance().getOrdersApi().getOrders(filter.getStart(), filter.getEnd(),
                        filter.getStatuses()))
                .invoke(response -> Optional.ofNullable(response.body())
                        .ifPresent(orderResponse -> mAdapter.setItems(Optional.of(orderResponse)
                                .orElse(new OrderResponse()).getOrders().stream()
                                .sorted(Comparator.comparing(Order::getOrderStatus)
                                        .thenComparing(Order::getDeadline))
                                .collect(Collectors.toList()))));
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Заказы");
        fillItems();
        binding.ordersList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        binding.orderCreate.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("order", new Order());
            Navigation.getNavigation(requireActivity()).goForward(new OrderGoodsFragment(), bundle);
        });
        mAdapter = new OrderRecyclerViewAdapter();
        mAdapter.setFilterPredicate(s -> order -> order.getClient().getName().toLowerCase().contains(s.toLowerCase()));
//        mAdapter.setEditItemListener(item -> new NetworkExecutorWrapper<>(requireActivity(),
//                NetworkService.getInstance().getOrdersApi().getOrder(item.getId()))
//                .invoke(orderResponse -> {
//                    if (orderResponse.isSuccessful()) {
//                        Bundle bundle = new Bundle();
//                        Order incomingOrder = Optional.ofNullable(orderResponse.body())
//                                .map(OrderResponse::getOrders)
//                                .filter(Objects::nonNull)
//                                .filter(f -> !f.isEmpty())
//                                .map(m -> m.iterator().next())
//                                .orElse(null);
//                        bundle.putSerializable("order", item);
//                        Navigation.getNavigation(requireActivity()).goForward(new OrderCardFragment(), bundle);
//                    }
//                }));
        mAdapter.setEditItemListener(item -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("order", item);
            Navigation.getNavigation(requireActivity()).goForward(new OrderCardFragment(), bundle);
        });
        binding.orderListFilter.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).goForward(new OrderFilterFragment()));
        binding.ordersList.setAdapter(mAdapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mAdapter.setEditItemListener(null);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}