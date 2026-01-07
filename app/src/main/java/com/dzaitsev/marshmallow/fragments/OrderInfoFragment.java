package com.dzaitsev.marshmallow.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.OrderSharedViewModel;
import com.dzaitsev.marshmallow.databinding.FragmentOrderInfoBinding;
import com.dzaitsev.marshmallow.dto.NsiItem;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.bundles.OrderCardBundle;
import com.dzaitsev.marshmallow.utils.EditTextUtil;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

import lombok.Getter;

public class OrderInfoFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "orderCardInfoFragment";

    @Getter
    private FragmentOrderInfoBinding binding;
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public OrderInfoFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void updateUI() {
        assert getView() != null;
        Order order = getOrderCardBundle().getOrder();
        OrderSharedViewModel viewModel = new ViewModelProvider(requireParentFragment())
                .get(OrderSharedViewModel.class);

        viewModel.getSumsChanged().observe(getViewLifecycleOwner(), unused -> bindSums());
        boolean editable = getOrderCardBundle().getOrder().getOrderStatus().isEditable();
        binding.etIssueDate.setEnabled(editable);
        binding.phoneNumber.setEnabled(editable);
        binding.tvAddress.setEnabled(editable);
        binding.cbDelivery.setEnabled(editable);
        binding.etComment.setEnabled(editable);
        binding.etPrePay.setEnabled(editable);
        binding.btnPaid.setEnabled(editable);
        if (order.getId() == null) {
            binding.tvClient.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("orderCardBundle", GsonHelper.serialize(getOrderCardBundle()));
                Navigation.getNavigation().forward(ClientsFragment.IDENTITY, bundle);
            });
        }
        binding.tvClient.setText(Optional.ofNullable(order.getClient())
                .map(NsiItem::getName)
                .orElse(null));
        EditTextUtil.setText(binding.phoneNumber, order.getPhone());
        EditTextUtil.setText(binding.etComment, order.getComment());
        EditTextUtil.setText(binding.tvAddress, order.getDeliveryAddress());
        binding.tvCreated.setText(dateTimeFormatter.format(order.getCreateDate()));
        binding.etIssueDate.setText(dateTimeFormatter.format(Optional.ofNullable(order.getDeadline())
                .orElseGet(() -> {
                    order.setDeadline(LocalDate.now().plusDays(1));
                    return order.getDeadline();
                })));
        binding.cbDelivery.setChecked(order.isNeedDelivery());
        binding.etPrePay.setText(MoneyUtils.moneyToString(Optional.ofNullable(order.getPrePaymentSum()).orElse(0d)));
        bindSums();
        binding.btnPaid.setOnClickListener(v -> {
            boolean paid = binding.btnPaid.isSelected();

            binding.btnPaid.setSelected(!paid);

            if (!paid) {
                binding.btnPaid.setText("Оплачено");
                order.setPaySum(calcToPay(getOrderCardBundle()));
                binding.etPrePay.setEnabled(false);
            } else {
                binding.btnPaid.setText("Оплатить");
                order.setPaySum(0d);
                binding.etPrePay.setEnabled(true);
            }
            bindSums();
        });
        binding.etIssueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        binding.etIssueDate.setText(String.format(Locale.getDefault(), "%02d.%02d.%d", dayOfMonth, month + 1, year));
                        order.setDeadline(LocalDate.of(year, month + 1, dayOfMonth));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
        binding.phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                getOrderCardBundle().getOrder().setPhone(binding.phoneNumber.getRawText());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        binding.etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                getOrderCardBundle().getOrder().setComment(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        binding.cbDelivery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getOrderCardBundle().getOrder().setNeedDelivery(isChecked);
            viewModel.notifyDeliveryChanged();

        });
        binding.etPrePay.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                double prePay = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                double totalSum = calcTotalSum(getOrderCardBundle());
                double toPay = totalSum - prePay;
                binding.tvToPay.setText(String.format(Locale.getDefault(), "%.0f ₽", toPay));
                order.setPrePaymentSum(prePay);
                bindSums();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private OrderCardBundle getOrderCardBundle() {
        Fragment parent = getParentFragment();
        if (parent instanceof OrderFragment orderFragment) {
            return orderFragment.getOrderCardBundle();
        }
        return GsonHelper.deserialize(requireArguments().getString("orderCardBundle"), OrderCardBundle.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentOrderInfoBinding.bind(view);
        updateUI();
    }

    private void bindSums() {
        binding.tvTotal.setText(MoneyUtils.moneyWithCurrencyToString(calcTotalSum(getOrderCardBundle())));
        Double calcToPay = calcToPay(getOrderCardBundle());
        binding.tvToPay.setText(MoneyUtils.moneyWithCurrencyToString(calcToPay));
        if (calcToPay.equals(0d)) {
            binding.tvToPay.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        }
        if (calcToPay > 0d) {
            binding.btnPaid.setSelected(false);
            binding.btnPaid.setText("Оплатить");
        } else {
            binding.btnPaid.setSelected(true);
            binding.btnPaid.setText("Оплачено");
        }
    }

    public static Double calcTotalSum(OrderCardBundle orderCardBundle) {
        return orderCardBundle.getOrderLines().stream()
                .mapToDouble(m -> Optional.ofNullable(m.getPrice()).orElse(0d) * Optional.ofNullable(m.getCount()).orElse(0))
                .sum();
    }

    public static Double calcToPay(OrderCardBundle orderCardBundle) {
        return calcTotalSum(orderCardBundle) - (Optional.ofNullable(orderCardBundle.getOrder().getPrePaymentSum()).orElse(0d)
                + Optional.ofNullable(orderCardBundle.getOrder().getPaySum()).orElse(0d));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_info, container, false);

    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}
