package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.DeliveryRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentDeliveriesBinding;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.response.DeliveryResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;

import java.util.Optional;
import java.util.stream.Collectors;

public class DeliveriesFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "deliveriesFragment";

    private FragmentDeliveriesBinding binding;

    private DeliveryRecyclerViewAdapter mAdapter;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentDeliveriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void fillItems() {
        new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().getDeliveries(),
                response -> Optional.ofNullable(response.body())
                        .ifPresent(deliveryReposponse -> {
                            mAdapter.setItems(Optional.of(deliveryReposponse)
                                    .orElse(new DeliveryResponse()).getDeliveries().stream()
                                    .sorted((o1, o2) -> {
                                        int i = o1.getDeliveryDate().compareTo(o2.getDeliveryDate());
                                        if (i == 0) {
                                            return o1.getStart().compareTo(o2.getStart());
                                        } else {
                                            return i;
                                        }
                                    }).collect(Collectors.toList()));

                        })).invoke();
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
        mAdapter.setEditItemListener(item -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("delivery", item);
            Navigation.getNavigation(requireActivity()).goForward(new DeliveryCardFragment(), bundle);
        });
        mAdapter.setDeleteItemListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Вы уверены?");
            builder.setPositiveButton("Да", (dialog, id) -> {
                NetworkExecutor<Void> callback = new NetworkExecutor<>(requireActivity(),
                        NetworkService.getInstance().getMarshmallowApi().deleteDelivery(item.getId()), response -> {
                }, true);
                callback.invoke();
                binding.deliveriesList.setAdapter(mAdapter);
                fillItems();
            });
            builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
            builder.create().show();
        });
        binding.deliveriesList.setAdapter(mAdapter);
        view.post(() -> {
            ColorStateList colorStateList = ColorStateList.valueOf(getBackgroundColor(view));
            binding.deliveryCreate.setBackgroundTintList(colorStateList);
        });
    }
    private int getBackgroundColor(View view) {
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable colorDrawable) {
            return colorDrawable.getColor();
        } else {
            return ContextCompat.getColor(requireContext(), R.color.white);
        }
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