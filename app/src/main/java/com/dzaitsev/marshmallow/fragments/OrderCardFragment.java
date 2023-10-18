package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.MainActivity;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.OrderLinesRecyclerViewAdapter;
import com.dzaitsev.marshmallow.components.DateTimePicker;
import com.dzaitsev.marshmallow.components.LinkChannelPicker;
import com.dzaitsev.marshmallow.components.MoneyPicker;
import com.dzaitsev.marshmallow.databinding.FragmentOrderCardBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.service.CallPhoneService;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.service.SendSmsService;
import com.dzaitsev.marshmallow.service.SendWhatsappService;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

public class OrderCardFragment extends Fragment {

    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private FragmentOrderCardBinding binding;
    private Order incomingOrder;
    private Order order;

    private OrderLinesRecyclerViewAdapter mAdapter;

    private final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> {
                    if (save()) {
                        setEnabled(false);
                        requireActivity().onBackPressed();
                    }
                });
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                });
                builder.create().show();
            } else {
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        }
    };

    private boolean hasChanges() {
        fillOrder();
        return !order.equals(incomingOrder);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        order = requireArguments().getSerializable("order", Order.class);
        incomingOrder = order.clone();
        binding = FragmentOrderCardBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private View.OnKeyListener keyListener = (v, keyCode, event) -> {
        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.field_background));
        return false;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        FragmentActivity fragmentActivity = requireActivity();
//        if (fragmentActivity instanceof MainActivity ma) {
//            ma.setNavigationBackListener(() -> {
//                if (hasChanges()) {
//                    requireActivity().onBackPressed();
//                    return false;
//                } else {
//                    return true;
//                }
//            });
//        }
//
//        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.clientName.setText(order.getClient().getName());
        binding.phoneNumber.setText(order.getPhone());
        binding.comment.setText(order.getComment());
        binding.delivery.setText(order.getDeliveryAddress());
        binding.prePayment.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(Optional.ofNullable(order.getPrePaymentSum()).orElse(0d)));
        binding.orderCardNeedDelivery.setChecked(order.isNeedDelivery());
        binding.deadline.setText(dateTimeFormatter.format(order.getDeadline()));

        binding.createDate.setText(dateTimeFormatter.format(order.getCreateDate()));
        bindSums();
        RecyclerView orderLinesList = view.findViewById(R.id.orderLinesList);
        orderLinesList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        if (order.isShipped()) {
            binding.orderCardLineAdd.setVisibility(View.GONE);
        } else {
            binding.orderCardLineAdd.setOnClickListener(v -> {
                OrderLine orderLine = new OrderLine();
                orderLine.setNum(mAdapter.getOriginalItems().stream().max(Comparator.comparing(OrderLine::getNum))
                        .map(OrderLine::getNum).map(m -> m + 1).orElse(1));
                mAdapter.addItem(orderLine);
            });
        }

        mAdapter = new OrderLinesRecyclerViewAdapter();
        if (!order.isShipped()) {
            mAdapter.setRemoveListener(position -> {
                if (position >= 0) {
                    for (int i = position; i < mAdapter.getOriginalItems().size(); i++) {
                        mAdapter.getOriginalItems().get(i).setNum(mAdapter.getOriginalItems().get(i).getNum() - 1);
                    }
                }
                orderLinesList.setAdapter(mAdapter);
                mAdapter.setItems(mAdapter.getOriginalItems());
                bindSums();
            });
            mAdapter.setSelectGoodListener(orderLine -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("order", order);
                bundle.putInt("orderline", orderLine.getNum());
                bundle.putString("source", "orderCard");
                NavHostFragment.findNavController(OrderCardFragment.this).navigate(R.id.action_orderCardFragment_to_goodsFragment, bundle);
            });
            mAdapter.setChangeSumListener(this::bindSums);

            binding.deadline.setOnClickListener(v -> {
                DateTimePicker dateTimePicker = new DateTimePicker(requireActivity(),
                        date -> {
                            order.setDeadline(date);
                            binding.deadline.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date));
                            binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                        }, "Выбор даты", "Укажите дату выдачи");
                dateTimePicker.show();
            });

            binding.prePayment.setOnClickListener(v -> MoneyPicker.builder(view.getContext())
                    .setTitle("Укажите сумму")
                    .setMinValue(1)
                    .setMaxValue(100000)
                    .positiveButton(value -> {
                        binding.prePayment.setText(String.format("%s", MoneyUtils.getInstance()
                                .moneyWithCurrencyToString(value)));
                        order.setPrePaymentSum(value);
                        bindSums();
                    })
                    .setInitialValue(order.getPrePaymentSum())
                    .build()
                    .show());
            binding.connect.setOnClickListener(v -> LinkChannelPicker.builder(v.getContext())
                    .setTitle("Выберите канал связи")
                    .setAction((alertDialog, linkChannel) -> {
                        String phone = String.format("+7%s", binding.phoneNumber.getRawText());
                        switch (linkChannel) {
                            case PHONE ->
                                    CallPhoneService.getInstance().call(requireContext(), phone);
                            case WHATSAPP ->
                                    SendWhatsappService.getInstance().send(requireContext(), phone, "");
                            case SMS ->
                                    SendSmsService.getInstance().sendSms(requireContext(), phone, "");
                        }
                        alertDialog.dismiss();
                    }).build()
                    .show());
            mAdapter.setDoneListener((orderLine, v) -> {
                orderLine.setDone(!orderLine.isDone());
                if (orderLine.isDone()) {
                    v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.green));
                } else {
                    mAdapter.getShowItems().stream().filter(f -> f.getNum().equals(orderLine.getNum()))
                            .findFirst()
                            .ifPresent(line -> {
                                if (mAdapter.getShowItems().indexOf(line) % 2 == 0) {
                                    v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.row_1));
                                } else {
                                    v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.row_2));
                                }
                            });
                }
            });
        } else {
            binding.phoneNumber.setInputType(InputType.TYPE_NULL);
            binding.delivery.setInputType(InputType.TYPE_NULL);
            binding.comment.setInputType(InputType.TYPE_NULL);
            binding.orderCardNeedDelivery.setEnabled(false);
        }
        if (calcToPay() == 0) {
            binding.orderCardPaid.setVisibility(View.GONE);
        }
        if (order.isShipped()) {
            binding.orderCardShipped.setVisibility(View.GONE);
        }
        if (order.getStatus() != OrderStatus.DONE) {
            binding.orderCardPaid.setVisibility(View.GONE);
            binding.orderCardShipped.setVisibility(View.GONE);
        }
        binding.orderCardPaid.setOnClickListener(v -> MoneyPicker.builder(requireContext())
                .setInitialValue(MoneyUtils.getInstance().stringToDouble(binding.toPay.getText().toString()))
                .setTitle("Оплата")
                .setMessage("Подтвердите сумму оплаты")
                .setMinValue(0)
                .positiveButton(sum -> {
                    order.setPaySum(Optional.ofNullable(order.getPaySum()).orElse(0d) + sum);
                    bindSums();
                }).build().show());
        binding.orderCardShipped.setOnClickListener(v -> {
            order.setShipped(true);
        });
        binding.orderCardCancel.setOnClickListener(v -> {
            if (!hasChanges()) {
                NavHostFragment.findNavController(OrderCardFragment.this).navigate(R.id.action_orderCardFragment_to_ordersFragment);
            }
        });

        binding.orderCardSave.setOnClickListener(v -> {
            if (save()) {
                if (order.getOrderLines().stream().allMatch(OrderLine::isDone) && order.getStatus() != OrderStatus.SHIPPED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Запись полностью выполнен");
                    builder.setMessage("Оповестить клиента?");
                    builder.setPositiveButton("Да", (dialog, id) -> {
                        sendNotification(order);
                        dialog.dismiss();
                    });
                    builder.setNegativeButton("Нет", (dialog, id) -> dialog.dismiss());
                    builder.create().show();
                }
                NavHostFragment.findNavController(OrderCardFragment.this).navigate(R.id.action_orderCardFragment_to_ordersFragment);
            }
        });

        orderLinesList.setAdapter(mAdapter);
        order.getOrderLines().sort(Comparator.comparing(OrderLine::getNum));
        mAdapter.setItems(order.getOrderLines());
    }

    private void sendNotification(Order order) {

    }

    private void bindSums() {
        binding.totalSum.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(calcTotalSum()));
        Double calcToPay = calcToPay();
        binding.toPay.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(calcToPay));
        binding.paid.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(order.getPaySum()));
        if (calcToPay.equals(0d)) {
            binding.toPay.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        }
    }

    private Double calcTotalSum() {
        return order.getOrderLines().stream()
                .mapToDouble(m -> Optional.ofNullable(m.getPrice()).orElse(0d) * Optional.ofNullable(m.getCount()).orElse(0))
                .sum();
    }

    private Double calcToPay() {
        return calcTotalSum() - (Optional.ofNullable(order.getPrePaymentSum()).orElse(0d)
                + Optional.ofNullable(order.getPaySum()).orElse(0d));
    }

    private boolean save() {
        boolean fail = false;
        fillOrder();
        if (order.getClient() == null) {
            binding.clientName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (StringUtils.isEmpty(order.getPhone())) {
            binding.phoneNumber.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (order.isNeedDelivery() && StringUtils.isEmpty(order.getDeliveryAddress())) {
            binding.delivery.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        mAdapter.getOriginalItems().removeIf(g -> g.getGood() == null);
        if (mAdapter.getOriginalItems().isEmpty()) {
            Toast.makeText(requireContext(), "Невозможно сохранить заказ. Он пуст", Toast.LENGTH_SHORT).show();
            fail = true;
        }
        if (fail) {
            return false;
        }
        NetworkExecutor<Void> callback = new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().saveOrder(order), response -> {
        }, true);
        callback.invoke();
        incomingOrder = order;
        return callback.isSuccess();
    }

    private void fillOrder() {
        order.setComment(binding.comment.getText().toString());
        order.setDeliveryAddress(binding.delivery.getText().toString());
        order.setPhone(binding.phoneNumber.getRawText());
        order.setNeedDelivery(binding.orderCardNeedDelivery.isChecked());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        FragmentActivity fragmentActivity = requireActivity();
        if (fragmentActivity instanceof MainActivity ma) {
            ma.setNavigationBackListener(null);
        }
    }

}