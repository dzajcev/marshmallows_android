package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.DatePicker;
import com.dzaitsev.marshmallow.components.MoneyPicker;
import com.dzaitsev.marshmallow.databinding.FragmentOrderClientBinding;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.bundles.OrderCardBundle;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.EditTextUtil;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OrderClientFragment extends Fragment implements IdentityFragment {

    public static final String IDENTITY = "orderClientFragment1";
    private FragmentOrderClientBinding binding;

    private OrderCardBundle orderCardBundle;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        orderCardBundle = GsonHelper.deserialize(requireArguments().getString("orderCardBundle"), OrderCardBundle.class);
        binding = FragmentOrderClientBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = new Bundle();
        requireActivity().setTitle("Информация по заказу");
        binding.ordersClientBackward.setOnClickListener(view1 -> {
            bundle.putString("orderCardBundle", GsonHelper.serialize(orderCardBundle));
            Navigation.getNavigation().back(bundle);
        });
        binding.ordersClientSave.setOnClickListener(view1 -> save());
        binding.clientName.setOnClickListener(v -> {
            bundle.putString("orderCardBundle", GsonHelper.serialize(orderCardBundle));
            Navigation.getNavigation().forward(ClientsFragment.IDENTITY, bundle);
        });
        binding.deadline.setOnClickListener(v -> {
            DatePicker datePicker = new DatePicker(requireActivity(),
                    date -> {
                        orderCardBundle.getOrder().setDeadline(date);
                        binding.deadline.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date));
                        binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                    }, "Выбор даты", "Укажите дату выдачи");
            datePicker.show();
        });
        binding.orderClientDelivery.setOnClickListener(v -> Toast.makeText(requireContext(), ((EditText) v).getText(), Toast.LENGTH_SHORT).show());
        binding.prePayment.setOnClickListener(v -> MoneyPicker.builder(view.getContext())
                .setTitle("Укажите сумму")
                .setMinValue(0)
                .positiveButton(value -> {
                    binding.prePayment.setText(String.format("%s", MoneyUtils.moneyWithCurrencyToString(value)));
                    orderCardBundle.getOrder().setPrePaymentSum(value);
                })
                .build()
                .show());
        bind();
    }

    private void bind() {
        EditTextUtil.setText(binding.orderClientDelivery, Optional.ofNullable(orderCardBundle.getOrder().getClient()).map(Client::getDefaultDeliveryAddress).orElse(""));
        EditTextUtil.setText(binding.phoneNumber, Optional.ofNullable(orderCardBundle.getOrder().getClient()).map(Client::getPhone).orElse(""));
        binding.clientName.setText(Optional.ofNullable(orderCardBundle.getOrder().getClient()).map(Client::getName).orElse(""));

        EditTextUtil.setText(binding.comment, Optional.ofNullable(orderCardBundle.getOrder().getComment()).orElse(""));
        binding.prePayment.setText(MoneyUtils.moneyWithCurrencyToString(orderCardBundle.getOrder().getPrePaymentSum()));
        binding.orderClientsNeedDelivery.setChecked(orderCardBundle.getOrder().isNeedDelivery());
        Optional.ofNullable(orderCardBundle.getOrder().getDeadline())
                .ifPresent(o -> {
                    binding.deadline.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(o));
                    binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                });
        binding.orderClientDelivery.requestLayout();
        binding.phoneNumber.requestLayout();
    }

    private void fillOrder() {
        orderCardBundle.getOrder().setComment(binding.comment.getText().toString());
        orderCardBundle.getOrder().setDeliveryAddress(binding.orderClientDelivery.getText().toString());
        orderCardBundle.getOrder().setPhone(binding.phoneNumber.getRawText());
        orderCardBundle.getOrder().setNeedDelivery(binding.orderClientsNeedDelivery.isChecked());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void save() {
        fillOrder();
        boolean fail = false;
        if (orderCardBundle.getOrder().getClient() == null) {
            binding.clientName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (orderCardBundle.getOrder().getDeadline() == null) {
            binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (orderCardBundle.getOrder().isNeedDelivery() && StringUtils.isEmpty(orderCardBundle.getOrder().getDeliveryAddress())) {
            binding.orderClientDelivery.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (fail) {
            return;
        }
        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getOrdersApi().saveOrder(orderCardBundle.getOrder()))
                .invoke(response -> Navigation.getNavigation().forward(OrdersFragment.IDENTITY));
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}