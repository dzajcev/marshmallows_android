package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.adapters.DeliveryRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentDeliveriesBinding;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.response.DeliveryResponse;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;
import com.dzaitsev.marshmallow.utils.orderfilter.FiltersHelper;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeliveriesFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "deliveriesFragment";

    private FragmentDeliveriesBinding binding;

    private DeliveryRecyclerViewAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentDeliveriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void fillItems() {
        FiltersHelper.getInstance().getDeliveryFilter()
                .ifPresent(filter -> new NetworkExecutorHelper<>(requireActivity(),
                        NetworkService.getInstance().getDeliveryApi().getDeliveries(filter.getStart(), filter.getEnd(), filter.getStatuses()))
                        .invoke(response -> Optional.ofNullable(response.body())
                                .ifPresent(deliveryReposponse -> mAdapter.setItems(Optional.of(deliveryReposponse)
                                        .orElse(new DeliveryResponse()).getDeliveries().stream()
                                        .sorted(Comparator.comparing(Delivery::getDeliveryStatus)
                                                .thenComparing(Delivery::getDeliveryDate)
                                                .thenComparing(Delivery::getStart))
                                        .collect(Collectors.toList())))));

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Доставки");
        fillItems();
        binding.deliveriesList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        binding.deliveryCreate.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putString("delivery", GsonHelper.serialize(new Delivery()));
            Navigation.getNavigation().goForward(new DeliveryCardFragment(), bundle);
        });
        mAdapter = new DeliveryRecyclerViewAdapter();
        mAdapter.setEditItemListener(item -> new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getDeliveryApi().getDelivery(item.getId()))
                .invoke(deliveryResponse -> {
                    if (deliveryResponse.isSuccessful()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("delivery", GsonHelper.serialize(deliveryResponse.body()));
                        Navigation.getNavigation().goForward(new DeliveryCardFragment(), bundle);
                    }
                }));
        binding.deliveriesList.setAdapter(mAdapter);
        binding.deliveryFilter.setOnClickListener(v -> Navigation.getNavigation().goForward(new DeliveryFilterFragment()));
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