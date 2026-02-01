package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.adapters.DeliveryRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentDeliverySelectorBinding;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.DeliveryStatus;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.bundles.OrderCardBundle;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class DeliverySelectorFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "deliverySelectorFragment";

    private FragmentDeliverySelectorBinding binding;
    private DeliveryRecyclerViewAdapter mAdapter;
    private OrderCardBundle orderCardBundle;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDeliverySelectorBinding.inflate(inflater, container, false);
        orderCardBundle = GsonHelper.deserialize(requireArguments().getString("orderCardBundle"), OrderCardBundle.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Привязать к доставке");

        mAdapter = new DeliveryRecyclerViewAdapter();
        binding.deliverySelectorItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.deliverySelectorItems.setAdapter(mAdapter);

        mAdapter.setSelectItemListener(delivery -> {
            addOrderToDelivery(delivery, orderCardBundle.getOrder());
        });

        binding.btnBack.setOnClickListener(v -> Navigation.getNavigation().back());

        loadDeliveries();
    }

    private void loadDeliveries() {
        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getDeliveryApi().getDeliveries(LocalDate.now(), null,
                        List.of(DeliveryStatus.NEW, DeliveryStatus.IN_PROGRESS)))
                .invoke(response -> {
                    if (response.isSuccessful() && response.body() != null) {
                        mAdapter.setItems(response.body().getData());
                    }
                });
    }

    private void addOrderToDelivery(Delivery delivery, Order order) {
        if (delivery.getOrders().stream().anyMatch(o -> Objects.equals(o.getId(), order.getId()))) {
            Toast.makeText(requireContext(), "Заказ уже в этой доставке", Toast.LENGTH_SHORT).show();
            return;
        }

        order.setNeedDelivery(true);
        delivery.getOrders().add(order);

        new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance().getDeliveryApi().saveDelivery(delivery))
                .invoke(response -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Заказ добавлен в доставку", Toast.LENGTH_SHORT).show();
                        
                        Bundle bundle = new Bundle();
                        bundle.putString("orderCardBundle", GsonHelper.serialize(orderCardBundle));
                        Navigation.getNavigation().back(bundle);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}
