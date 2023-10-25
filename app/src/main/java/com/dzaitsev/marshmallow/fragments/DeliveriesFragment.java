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
import com.dzaitsev.marshmallow.adapters.DeliveryRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentDeliveriesBinding;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.DeliveryFilter;
import com.dzaitsev.marshmallow.dto.response.DeliveryResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutorWrapper;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeliveriesFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "deliveriesFragment";

    private FragmentDeliveriesBinding binding;

    private DeliveryRecyclerViewAdapter mAdapter;

    private DeliveryFilter filter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String string = preferences.getString("delivery-filter", "{}");
        if (string.isEmpty()) {
            string = "{}";
        }
        Gson gson = GsonExt.getGson();
        Type type = new TypeToken<DeliveryFilter>() {
        }.getType();
        filter = gson.fromJson(string, type);
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
        new NetworkExecutorWrapper<>(requireActivity(),
                NetworkService.getInstance().getDeliveryApi().getDeliveries(filter.getStart(), filter.getEnd(), filter.getStatuses()))
                .invoke(response -> Optional.ofNullable(response.body())
                        .ifPresent(deliveryReposponse -> mAdapter.setItems(Optional.of(deliveryReposponse)
                                .orElse(new DeliveryResponse()).getDeliveries().stream()
                                .sorted(Comparator.comparing(Delivery::getStatus)
                                        .thenComparing(Delivery::getDeliveryDate)
                                        .thenComparing(Delivery::getStart))
                                .collect(Collectors.toList()))));
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Доставки");
        fillItems();
        binding.deliveriesList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        binding.deliveryCreate.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("delivery", new Delivery());
            Navigation.getNavigation(requireActivity()).goForward(new DeliveryCardFragment(), bundle);
        });
        mAdapter = new DeliveryRecyclerViewAdapter();
        mAdapter.setEditItemListener(item -> new NetworkExecutorWrapper<>(requireActivity(),
                NetworkService.getInstance().getDeliveryApi().getDelivery(item.getId()))
                .invoke(deliveryResponse -> {
                    if (deliveryResponse.isSuccessful()) {
                        Delivery delivery = Optional.ofNullable(deliveryResponse.body())
                                .map(DeliveryResponse::getDeliveries)
                                .filter(Objects::nonNull)
                                .filter(f -> !f.isEmpty())
                                .map(m -> m.iterator().next())
                                .orElse(null);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("delivery", delivery);
                        Navigation.getNavigation(requireActivity()).goForward(new DeliveryCardFragment(), bundle);
                    }
                }));
        binding.deliveriesList.setAdapter(mAdapter);
        binding.deliveryFilter.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).goForward(new DeliveryFilterFragment()));
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