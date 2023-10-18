package com.dzaitsev.marshmallow.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.DateTimePicker;
import com.dzaitsev.marshmallow.components.MoneyPicker;
import com.dzaitsev.marshmallow.databinding.FragmentOrderClientBinding;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OrderClientFragment extends Fragment {
    private FragmentOrderClientBinding binding;

    private Order order;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        order = requireArguments().getSerializable("order", Order.class);
        binding = FragmentOrderClientBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = new Bundle();

        binding.ordersClientBackward.setOnClickListener(view1 -> {
            bundle.putSerializable("order", order);
            NavHostFragment.findNavController(OrderClientFragment.this)
                    .navigate(R.id.action_orderClientFragment_to_orderGoodsFragment, bundle);
        });
        binding.ordersClientSave.setOnClickListener(view1 -> {
            if (save()) {
                NavHostFragment.findNavController(OrderClientFragment.this)
                        .navigate(R.id.action_orderClientFragment_to_ordersFragment, bundle);
            }
        });
        binding.clientName.setOnClickListener(v -> {
            bundle.putSerializable("order", order);
            NavHostFragment.findNavController(OrderClientFragment.this)
                    .navigate(R.id.action_orderClientFragment_to_clientsFragment, bundle);
        });
        binding.deadline.setOnClickListener(v -> {
            DateTimePicker dateTimePicker = new DateTimePicker(requireActivity(),
                    date -> {
                        order.setDeadline(date);
                        binding.deadline.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date));
                        binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                    }, "Выбор даты", "Укажите дату выдачи");
            dateTimePicker.show();
        });
        binding.delivery.setOnClickListener(v -> binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background)));
        binding.prePayment.setOnClickListener(v -> MoneyPicker.builder(view.getContext())
                .setTitle("Укажите сумму")
                .setMinValue(1)
                .setMaxValue(100000)
                .positiveButton(value -> {
                    binding.prePayment.setText(String.format("%s", MoneyUtils.getInstance()
                            .moneyWithCurrencyToString(value)));
                    order.setPrePaymentSum(value);
                })
                .build()
                .show());
        bind(order);
    }

    private void bind(Order order) {
        binding.clientName.setText(Optional.ofNullable(order.getClient()).map(Client::getName).orElse(""));
        binding.phoneNumber.setText(Optional.ofNullable(order.getClient()).map(Client::getPhone).orElse(""));
        binding.delivery.setText(Optional.ofNullable(order.getDeliveryAddress()).orElse(""));
        binding.comment.setText(Optional.ofNullable(order.getComment()).orElse(""));
        binding.prePayment.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(order.getPrePaymentSum()));
        binding.orderClientsNeedDelivery.setChecked(order.isNeedDelivery());
        Optional.ofNullable(order.getDeadline())
                .ifPresent(o -> {
                    binding.deadline.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(o));
                    binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                });
    }

    private void fillOrder() {
        order.setComment(binding.comment.getText().toString());
        order.setDeliveryAddress(binding.delivery.getText().toString());
        order.setPhone(binding.phoneNumber.getRawText());
        order.setNeedDelivery(binding.orderClientsNeedDelivery.isChecked());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean save() {
        fillOrder();
        boolean fail = false;
        if (order.getClient() == null) {
            binding.clientName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (order.getDeadline() == null) {
            binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (order.isNeedDelivery() && StringUtils.isEmpty(order.getDeliveryAddress())) {
            binding.delivery.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (fail) {
            return false;
        }
        NetworkExecutor<Void> callback = new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().saveOrder(order), response -> {
        }, true);
        callback.invoke();
        return callback.isSuccess();
    }

}