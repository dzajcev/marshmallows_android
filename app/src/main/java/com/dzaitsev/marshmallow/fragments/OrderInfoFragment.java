package com.dzaitsev.marshmallow.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arefbhrn.maskededittext.MaskedEditText;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.OrderSharedViewModel;
import com.dzaitsev.marshmallow.dto.NsiItem;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.bundles.OrderCardBundle;
import com.dzaitsev.marshmallow.utils.EditTextUtil;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

public class OrderInfoFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "orderCardInfoFragment";
    private TextView tvClient;
    private TextView tvCreated;
    private TextView etIssueDate;
    private MaskedEditText tvPhone;

    private EditText tvAddress;
    private CheckBox cbDelivery;

    private ImageButton btnShare;
    private MaterialButton btnPaid;

    private TextView tvTotal;

    private TextView etPrePay;

    private TextView tvToPay;

    private EditText etComment;

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
        etIssueDate.setEnabled(editable);
        tvPhone.setEnabled(editable);
        tvAddress.setEnabled(editable);
        cbDelivery.setEnabled(editable);
        etComment.setEnabled(editable);
        etPrePay.setEnabled(editable);
        btnPaid.setEnabled(editable);
        if (order.getId() == null) {
            tvClient.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("orderCardBundle", GsonHelper.serialize(getOrderCardBundle()));
                Navigation.getNavigation().forward(ClientsFragment.IDENTITY, bundle);
            });
        }
        tvClient.setText(Optional.ofNullable(order.getClient())
                .map(NsiItem::getName)
                .orElse(null));
        EditTextUtil.setText(tvPhone, order.getPhone());
        EditTextUtil.setText(etComment, order.getComment());
        EditTextUtil.setText(tvAddress, order.getDeliveryAddress());
        tvCreated.setText(dateTimeFormatter.format(order.getCreateDate()));
        etIssueDate.setText(dateTimeFormatter.format(Optional.ofNullable(order.getDeadline())
                .orElseGet(() -> {
                    order.setDeadline(LocalDate.now().plusDays(1));
                    return order.getDeadline();
                })));
        cbDelivery.setChecked(order.isNeedDelivery());
        etPrePay.setText(MoneyUtils.moneyToString(Optional.ofNullable(order.getPrePaymentSum()).orElse(0d)));
        bindSums();
        btnPaid.setOnClickListener(v -> {
            boolean paid = btnPaid.isSelected();

            btnPaid.setSelected(!paid);

            if (!paid) {
                btnPaid.setText("Оплачено");
                order.setPaySum(calcToPay(getOrderCardBundle()));
                etPrePay.setEnabled(false);
            } else {
                btnPaid.setText("Оплатить");
                order.setPaySum(0d);
                etPrePay.setEnabled(true);
            }
            bindSums();
        });
        etIssueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        etIssueDate.setText(String.format(Locale.getDefault(), "%02d.%02d.%d", dayOfMonth, month + 1, year));
                        order.setDeadline(LocalDate.of(year, month + 1, dayOfMonth));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
        tvPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                getOrderCardBundle().getOrder().setPhone(tvPhone.getRawText());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        etComment.addTextChangedListener(new TextWatcher() {
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
        cbDelivery.setOnCheckedChangeListener((buttonView, isChecked) -> getOrderCardBundle().getOrder().setNeedDelivery(isChecked));
        etPrePay.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                double prePay = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                double totalSum = calcTotalSum(getOrderCardBundle());
                double toPay = totalSum - prePay;
                tvToPay.setText(String.format(Locale.getDefault(), "%.0f ₽", toPay));
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
//        throw new IllegalStateException("Parent must be OrderFragment");
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvClient = view.findViewById(R.id.tvClient);
        tvPhone = view.findViewById(R.id.phoneNumber);
        tvCreated = view.findViewById(R.id.tvCreated);
        etIssueDate = view.findViewById(R.id.etIssueDate);
        tvAddress = view.findViewById(R.id.tvAddress);
        cbDelivery = view.findViewById(R.id.cbDelivery);
        btnShare = view.findViewById(R.id.btnShare);
        tvTotal = view.findViewById(R.id.tvTotal);
        etPrePay = view.findViewById(R.id.etPrePay);
        tvToPay = view.findViewById(R.id.tvToPay);
        etComment = view.findViewById(R.id.etComment);
        btnPaid = view.findViewById(R.id.btnPaid);

        updateUI();
    }

    private void bindSums() {
        tvTotal.setText(MoneyUtils.moneyWithCurrencyToString(calcTotalSum(getOrderCardBundle())));
        Double calcToPay = calcToPay(getOrderCardBundle());
        tvToPay.setText(MoneyUtils.moneyWithCurrencyToString(calcToPay));
        if (calcToPay.equals(0d)) {
            tvToPay.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        }
        if (calcToPay > 0d) {
            btnPaid.setSelected(false);
            btnPaid.setText("Оплатить");
        } else {
            btnPaid.setSelected(true);
            btnPaid.setText("Оплачено");
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