package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.OrderRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentOrdersBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.OrderResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;

import java.util.Optional;
import java.util.stream.Collectors;

public class OrdersFragment extends Fragment implements Identity{

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

    private void fillItems() {
        new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().getOrders(),
                response -> Optional.ofNullable(response.body())
                        .ifPresent(orderResponse -> {
                            mAdapter.setItems(Optional.of(orderResponse)
                                    .orElse(new OrderResponse()).getOrders().stream()
                                    .sorted((o1, o2) -> {
                                        int i = o1.getDeadline().compareTo(o2.getDeadline());
                                        if (i == 0) {
                                            return o1.getId().compareTo(o2.getId());
                                        } else {
                                            return i;
                                        }
                                    }).collect(Collectors.toList()));

                        })).invoke();
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
        mAdapter.setEditItemListener(item -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("order", item);
            Navigation.getNavigation(requireActivity()).goForward(new OrderCardFragment(), bundle);
        });
        mAdapter.setDeleteItemListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Вы уверены?");
            builder.setPositiveButton("Да", (dialog, id) -> {
                NetworkExecutor<Void> callback = new NetworkExecutor<>(requireActivity(),
                        NetworkService.getInstance().getMarshmallowApi().deleteOrder(item.getId()), response -> {
                }, true);
                callback.invoke();
                binding.ordersList.setAdapter(mAdapter);
                fillItems();
            });
            builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
            builder.create().show();
        });
        binding.ordersList.setAdapter(mAdapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public String getUniqueName() {
        return getClass().getSimpleName();
    }
}