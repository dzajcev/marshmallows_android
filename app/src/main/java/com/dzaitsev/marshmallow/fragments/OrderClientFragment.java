package com.dzaitsev.marshmallow.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.DatePicker;
import com.dzaitsev.marshmallow.components.MoneyPicker;
import com.dzaitsev.marshmallow.databinding.FragmentOrderClientBinding;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.EditTextUtil;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OrderClientFragment extends Fragment implements IdentityFragment {

    public static final String IDENTITY = "orderClientFragment";
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
        requireActivity().setTitle("Информация по заказу");
        binding.ordersClientBackward.setOnClickListener(view1 -> {
            bundle.putSerializable("order", order);
            Navigation.getNavigation(requireActivity()).back();
        });
        binding.ordersClientSave.setOnClickListener(view1 -> {
            if (save()) {
                Navigation.getNavigation(requireActivity()).goForward(new OrdersFragment(), bundle);
            }
        });
        binding.clientName.setOnClickListener(v -> {
            bundle.putSerializable("order", order);
            Navigation.getNavigation(requireActivity()).goForward(new ClientsFragment(), bundle);
        });
        binding.deadline.setOnClickListener(v -> {
            DatePicker datePicker = new DatePicker(requireActivity(),
                    date -> {
                        order.setDeadline(date);
                        binding.deadline.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date));
                        binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                    }, "Выбор даты", "Укажите дату выдачи");
            datePicker.show();
        });
        binding.orderClientDelivery.setOnClickListener(v -> Toast.makeText(requireContext(), ((EditText) v).getText(), Toast.LENGTH_SHORT).show());
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
        EditTextUtil.setText(binding.orderClientDelivery, Optional.ofNullable(order.getClient()).map(Client::getDefaultDeliveryAddress).orElse(""));
        EditTextUtil.setText(binding.phoneNumber, Optional.ofNullable(order.getClient()).map(Client::getPhone).orElse(""));
        binding.clientName.setText(Optional.ofNullable(order.getClient()).map(Client::getName).orElse(""));

        EditTextUtil.setText(binding.comment,Optional.ofNullable(order.getComment()).orElse(""));
        binding.prePayment.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(order.getPrePaymentSum()));
        binding.orderClientsNeedDelivery.setChecked(order.isNeedDelivery());
        Optional.ofNullable(order.getDeadline())
                .ifPresent(o -> {
                    binding.deadline.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(o));
                    binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                });
        binding.orderClientDelivery.requestLayout();
        binding.phoneNumber.requestLayout();
    }

    private void fillOrder() {
        order.setComment(binding.comment.getText().toString());
        order.setDeliveryAddress(binding.orderClientDelivery.getText().toString());
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
            binding.orderClientDelivery.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
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

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}