package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.adapters.listeners.DeliveryExecutorSelectorRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentDeliveryExecutorBinding;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.User;
import com.dzaitsev.marshmallow.dto.response.DeliverymenResponse;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeliveryExecutorFragment extends Fragment implements IdentityFragment {

    public static final String IDENTITY = "deliveryExecutorFragment";

    private FragmentDeliveryExecutorBinding binding;
    private DeliveryExecutorSelectorRecyclerViewAdapter mAdapter;

    private final boolean multiSelectMode;

    public DeliveryExecutorFragment() {
        this.multiSelectMode = false;
    }

    public DeliveryExecutorFragment(boolean multiSelectMode) {
        this.multiSelectMode = multiSelectMode;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentDeliveryExecutorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Выбор исполнителя");
        Delivery delivery = Optional.ofNullable(getArguments())
                .map(m -> GsonHelper.deserialize(m.getString("delivery"), Delivery.class)).orElse(new Delivery());
        mAdapter = new DeliveryExecutorSelectorRecyclerViewAdapter(this.multiSelectMode);
        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getInviteRequestsApi().getDeliverymen()).invoke(response -> Optional.ofNullable(response.body())
                .ifPresent(orderResponse -> mAdapter.setItems(Optional.of(orderResponse)
                        .orElse(new DeliverymenResponse()).getUsers().stream()
                        .sorted(Comparator.comparing(User::getFullName))
                        .collect(Collectors.toList()))));
        if (!multiSelectMode) {
            mAdapter.setOnSelectListener(user -> {
                Bundle bundle = new Bundle();
                delivery.setExecutor(user);
                bundle.putString("delivery", GsonHelper.serialize(delivery));
                Navigation.getNavigation().back(bundle);
            });
        }
        binding.deliveryExecutorsItems.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.deliveryExecutorsItems.setAdapter(mAdapter);
        binding.deliveryExecutorSelectorCancel.setOnClickListener(v -> Navigation.getNavigation().back());
    }


    public List<User> getSelected() {
        return mAdapter.getSelectedUsers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
        binding = null;
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}