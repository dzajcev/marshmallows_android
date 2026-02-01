package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dzaitsev.marshmallow.components.OrderSharedViewModel;
import com.dzaitsev.marshmallow.databinding.FragmentOrderBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.dto.bundles.OrderCardBundle;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Optional;

import lombok.Getter;

public class OrderFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "orderCardFragment";

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }

    private FragmentOrderBinding binding;
    private Order incomingOrder;
    private OrderSharedViewModel viewModel;

    @Getter
    private OrderCardBundle orderCardBundle;
    private boolean isViewCreated = false;

    Navigation.OnBackListener backListener = fragment -> {
        if (OrderFragment.IDENTITY.equals(fragment.identity())) {
            // Обработка возврата из других экранов (например, селектора доставки)
            if (fragment.args() != null && fragment.args().containsKey("orderCardBundle")) {
                OrderCardBundle updatedBundle = GsonHelper.deserialize(fragment.args().getString("orderCardBundle"), OrderCardBundle.class);
                if (updatedBundle != null) {
                    this.orderCardBundle = updatedBundle;
                    viewModel.setOrder(updatedBundle.getOrder()); // Уведомляем InfoFragment об изменениях
                }
            }

            if (OrderFragment.this.hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderFragment.this.getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> OrderFragment.this.save(true));
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> Navigation.getNavigation().back());
                builder.create().show();
            } else {
                Navigation.getNavigation().back();
            }
        }
        return false;
    };

    public OrderFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(this).get(OrderSharedViewModel.class);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (orderCardBundle != null && orderCardBundle.getOrder().getId() != null) {
            MenuItem deleteOrder = menu.add("Удалить");
            deleteOrder.setOnMenuItemClickListener(item -> {
                double prePay = Optional.ofNullable(orderCardBundle.getOrder().getPrePaymentSum()).orElse(0d);
                if (prePay > 0) {
                    Toast.makeText(requireContext(), "Невозможно удалить заказ с предоплатой", Toast.LENGTH_SHORT).show();
                    return false;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Вы уверены?");
                builder.setPositiveButton("Да", (dialog, id) -> new NetworkExecutorHelper<>(requireActivity(),
                        NetworkService.getInstance().getOrdersApi().deleteOrder(orderCardBundle.getOrder().getId()))
                        .invoke(response -> {
                            if (response.isSuccessful()) {
                                Navigation.getNavigation().back();
                            }
                        }));
                builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
                builder.create().show();
                return false;
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);

        orderCardBundle = GsonHelper.deserialize(requireArguments().getString("orderCardBundle"), OrderCardBundle.class);
        viewModel.setOrder(orderCardBundle.getOrder());

        if (orderCardBundle.getOrder().getId() != null) {
            new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance().getOrdersApi().getOrder(orderCardBundle.getOrder().getId()))
                    .invoke(deliveryResponse -> {
                        if (deliveryResponse.isSuccessful() && deliveryResponse.body() != null) {
                            incomingOrder = deliveryResponse.body().getData();
                            orderCardBundle.setOrder(incomingOrder);
                            viewModel.setOrder(incomingOrder);
                        }
                    });
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        tryUpdateUI();
    }

    private boolean hasChanges() {
        orderCardBundle.getOrderLines()
                .removeIf(orderLine -> orderLine.getGood() == null || orderLine.getCount() == 0);

        if (incomingOrder == null && !orderCardBundle.getOrderLines().isEmpty()) {
            return true;
        }
        return incomingOrder != null && !incomingOrder.equals(orderCardBundle.getOrder());
    }

    private void save(boolean withNotification) {
        orderCardBundle.getOrder().setOrderLines(orderCardBundle.getOrderLines());

        if (orderCardBundle.getOrder().getOrderStatus() != OrderStatus.ISSUED) {
            if (orderCardBundle.getOrder().getOrderLines().stream().allMatch(OrderLine::isDone)) {
                orderCardBundle.getOrder().setOrderStatus(OrderStatus.DONE);
            } else {
                orderCardBundle.getOrder().setOrderStatus(OrderStatus.IN_PROGRESS);
            }
        }

        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getOrdersApi().saveOrder(orderCardBundle.getOrder())).invoke(response -> {
            Navigation.getNavigation().back();
        });
    }

    private void tryUpdateUI() {
        if (!isAdded() || !isViewCreated || binding == null) return;
        updateUI();
    }

    private void updateUI() {
        requireActivity().setTitle("Заказ");
        binding.saveButton.setOnClickListener(v -> save(true));
        binding.btnIssue.setOnClickListener(v -> {
            if (OrderInfoFragment.calcToPay(getOrderCardBundle()) > 0) {
                Toast.makeText(requireContext(), "Невозможно выдать заказ. Не оплачен", Toast.LENGTH_SHORT).show();
            } else {
                orderCardBundle.getOrder().setOrderStatus(OrderStatus.ISSUED);
                save(false);
            }
        });

        binding.btnIssue.setEnabled(getOrderCardBundle().getOrderLines().stream()
                .allMatch(OrderLine::isDone) && getOrderCardBundle().getOrder().getOrderStatus() == OrderStatus.DONE);
        binding.saveButton.setEnabled(getOrderCardBundle().getOrder().getOrderStatus().isEditable());

        viewModel.getDoneChanged().observe(getViewLifecycleOwner(), unused -> binding.btnIssue.setEnabled(!getOrderCardBundle().getOrder()
                .isNeedDelivery() && getOrderCardBundle().getOrderLines().stream()
                .allMatch(OrderLine::isDone)));

        Navigation.getNavigation().addOnBackListener(backListener);
        OrderTabsPagerAdapter adapter = new OrderTabsPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(orderCardBundle.getActiveTab(), false);
        binding.viewPager.setOffscreenPageLimit(2);

        new TabLayoutMediator(binding.orderTabLayout, binding.viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Информация" : "Позиции")
        ).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.viewPager.setAdapter(null);
            binding = null;
        }
        isViewCreated = false;
    }

    public static class OrderTabsPagerAdapter extends FragmentStateAdapter {
        public OrderTabsPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) return new OrderInfoFragment();
            else return new OrderLinesFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
