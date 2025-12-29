package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dzaitsev.marshmallow.components.OrderSharedViewModel;
import com.dzaitsev.marshmallow.databinding.FragmentOrderBinding;
import com.dzaitsev.marshmallow.databinding.FragmentOrderInfoBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.dto.bundles.OrderCardBundle;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;
import com.google.android.material.tabs.TabLayoutMediator;

import lombok.Getter;

public class OrderFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "orderCardFragment";

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }

    private FragmentOrderBinding binding;
    private Order incomingOrder;

    @Getter
    private OrderCardBundle orderCardBundle;
    private boolean isViewCreated = false;

    Navigation.OnBackListener backListener = fragment -> {
        if (OrderFragment.IDENTITY.equals(fragment.identity())) {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);

        orderCardBundle = GsonHelper.deserialize(requireArguments().getString("orderCardBundle"), OrderCardBundle.class);
        if (orderCardBundle.getOrder().getId() != null) {
            new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance().getOrdersApi().getOrder(orderCardBundle.getOrder().getId()))
                    .invoke(deliveryResponse -> {
                        if (deliveryResponse.isSuccessful()) {
                            incomingOrder = deliveryResponse.body();
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

        if (incomingOrder == null) {
            return true;
        }
        return !incomingOrder.equals(orderCardBundle.getOrder());
    }

    private boolean validateData() {
        OrderInfoFragment fragment = (OrderInfoFragment) getChildFragmentManager().getFragments().get(0);
        FragmentOrderInfoBinding infoBinding = fragment.getBinding();
        infoBinding.clientLayout.setError(null);
        infoBinding.phoneLayout.setError(null);
        infoBinding.addressLayout.setError(null);

        boolean isValid = true;
        if (orderCardBundle.getOrder().getClient() == null) {
            infoBinding.clientLayout.setError("Выберите клиента");
            isValid = false;
        }

        if (StringUtils.isEmpty(orderCardBundle.getOrder().getPhone()) || orderCardBundle.getOrder().getPhone().length() != 10) {
            infoBinding.phoneLayout.setError("Некорректный номер телефона");
            isValid = false;
        }

        if (orderCardBundle.getOrder().isNeedDelivery() && StringUtils.isEmpty(orderCardBundle.getOrder().getDeliveryAddress())) {
            infoBinding.addressLayout.setError("Укажите адрес доставки");
            isValid = false;
        }

        orderCardBundle.getOrderLines().removeIf(g -> g.getGood() == null);
        if (orderCardBundle.getOrderLines().isEmpty()) {
            Toast.makeText(requireContext(), "Невозможно сохранить заказ. Он пуст", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void save(boolean withNotification) {
        if (!validateData()) {
            return;
        }

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
            if (withNotification && orderCardBundle.getOrder().getOrderStatus() == OrderStatus.DONE) {
                new NetworkExecutorHelper<>(requireActivity(),
                        NetworkService.getInstance().getOrdersApi().clientIsNotificated(orderCardBundle.getOrder().getId())).invoke(booleanResponse -> {
                    if (Boolean.FALSE.equals(booleanResponse.body())) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                        builder.setTitle("Заказ полностью выполнен");
                        builder.setMessage("Оповестить клиента?");
                        builder.setPositiveButton("Да", (dialog, id) -> {
                            sendNotification(orderCardBundle.getOrder());
                            Navigation.getNavigation().back();
                            dialog.dismiss();
                        });
                        builder.setNegativeButton("Нет", (dialog, id) -> {
                            Navigation.getNavigation().back();
                            dialog.dismiss();
                        });
                        builder.create().show();
                    } else {
                        Navigation.getNavigation().back();
                    }
                });
            } else {
                Navigation.getNavigation().back();
            }
        });

    }

    private void sendNotification(Order order) {
//        new NetworkExecutorWrapper<>(NetworkService.getInstance().getOrdersApi().setClientIsNotificated(order.getId()))
//                .invokeSync();
//todo:
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
        OrderSharedViewModel viewModel = new ViewModelProvider(this)
                .get(OrderSharedViewModel.class);
        viewModel.setOrder(orderCardBundle.getOrder());
        binding.btnIssue.setEnabled(getOrderCardBundle().getOrderLines().stream()
                .allMatch(OrderLine::isDone) && getOrderCardBundle().getOrder().getOrderStatus() == OrderStatus.DONE);
        binding.saveButton.setEnabled(getOrderCardBundle().getOrder().getOrderStatus().isEditable());
        viewModel.getDoneChanged().observe(getViewLifecycleOwner(), unused -> binding.btnIssue.setEnabled(!getOrderCardBundle().getOrder()
                .isNeedDelivery() && getOrderCardBundle().getOrderLines().stream()
                .allMatch(OrderLine::isDone)));
        viewModel.getDeliveryChanged().observe(getViewLifecycleOwner(), unused -> binding.btnIssue.setEnabled(!getOrderCardBundle().getOrder()
                .isNeedDelivery() && getOrderCardBundle().getOrderLines().stream()
                .allMatch(OrderLine::isDone)));
        Navigation.getNavigation().addOnBackListener(backListener);
        OrderTabsPagerAdapter adapter = new OrderTabsPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(orderCardBundle.getActiveTab(), false);
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
