package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.adapters.OrderSelectorRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentOrderSelectorBinding;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.OrderResponse;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderSelectorFragment extends Fragment implements IdentityFragment {

    public static final String IDENTITY = "orderSelectorFragment";

    private FragmentOrderSelectorBinding binding;
    private OrderSelectorRecyclerViewAdapter mAdapter;

    private final Navigation.OnBackListener backListener = fragment -> {
        if (OrderSelectorFragment.this == fragment) {
            if (OrderSelectorFragment.this.hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderSelectorFragment.this.getActivity());
                builder.setTitle("Сохранить выбор?");
                builder.setPositiveButton("Да", (dialog, id) -> {
                    //todo:
//                    if (DeliveryCardFragment.this.save()) {
//                        Navigation.getNavigation(DeliveryCardFragment.this.requireActivity()).back();
//                    }
                });
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> Navigation.getNavigation().back());
                builder.create().show();
            } else {
                Navigation.getNavigation().back();
            }
        }
        return false;
    };

    private boolean hasChanges() {
        return false;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentOrderSelectorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Navigation.getNavigation().addOnBackListener(backListener);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Выбор заказов");
        Delivery delivery = Optional.ofNullable(getArguments())
                .map(m -> GsonHelper.deserialize(m.getString("delivery"), Delivery.class)).orElse(new Delivery());
        mAdapter = new OrderSelectorRecyclerViewAdapter();
        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getOrdersApi().getOrdersForDelivery()).invoke(response -> Optional.ofNullable(response.body())
                .ifPresent(orderResponse -> mAdapter.setItems(Optional.of(orderResponse)
                        .orElse(new OrderResponse()).getOrders().stream()
                        .filter(f -> delivery.getOrders().stream().noneMatch(f1 -> f1.getId().equals(f.getId())))
                        .sorted(Comparator.comparing(Order::getOrderStatus)
                                .thenComparing(p -> p.getClient().getName()))
                        .collect(Collectors.toList()))));
        binding.orderSelectorItems.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.orderSelectorItems.setAdapter(mAdapter);
        binding.orderSelectorCancel.setOnClickListener(v -> Navigation.getNavigation().callbackBack());

        binding.orderSelectorSave.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            delivery.getOrders().addAll(getSelected());
            bundle.putString("delivery", GsonHelper.serialize(delivery));
            Navigation.getNavigation().back(bundle);
        });
    }

    public List<Order> getSelected() {
        return mAdapter.getSelectedOrders();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
        binding = null;
        Navigation.getNavigation().removeOnBackListener(backListener);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}