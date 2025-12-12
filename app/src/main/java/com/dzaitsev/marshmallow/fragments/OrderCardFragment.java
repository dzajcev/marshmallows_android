package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.OrderLinesRecyclerViewAdapter;
import com.dzaitsev.marshmallow.components.DatePicker;
import com.dzaitsev.marshmallow.components.LinkChannelPicker;
import com.dzaitsev.marshmallow.components.MoneyPicker;
import com.dzaitsev.marshmallow.databinding.FragmentOrderCardBinding;
import com.dzaitsev.marshmallow.databinding.FragmentOrderClientBinding;
import com.dzaitsev.marshmallow.databinding.FragmentOrderNewBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.service.CallPhoneService;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.service.SendSmsService;
import com.dzaitsev.marshmallow.service.SendWhatsappService;
import com.dzaitsev.marshmallow.utils.EditTextUtil;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

public class OrderCardFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "orderCardFragment";

    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private FragmentOrderNewBinding binding;
    private Order incomingOrder;
    private Order order;

    private OrderLinesRecyclerViewAdapter mAdapter;
    Navigation.OnBackListener backListener = fragment -> {
        if (OrderCardFragment.this == fragment) {
            if (OrderCardFragment.this.hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderCardFragment.this.getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> OrderCardFragment.this.save());
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> Navigation.getNavigation().back());
                builder.create().show();
            } else {
                Navigation.getNavigation().back();
            }
        }
        return false;
    };

    private boolean hasChanges() {
        fillOrder();
        return !order.equals(incomingOrder);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        order = GsonHelper.deserialize(requireArguments().getString("order"), Order.class);
        if (order.getId() != null) {
            new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance().getOrdersApi().getOrder(order.getId()))
                    .invoke(deliveryResponse -> {
                        if (deliveryResponse.isSuccessful()) {
                            incomingOrder = deliveryResponse.body();
                        }
                    });
        }
        requireActivity().setTitle("Информация о заказе");
        binding = FragmentOrderNewBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        MenuItem deleteOrder = menu.add("Удалить");
        deleteOrder.setOnMenuItemClickListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Вы уверены?");
            builder.setPositiveButton("Да", (dialog, id) -> new NetworkExecutorHelper<>(requireActivity(),
                    NetworkService.getInstance().getOrdersApi().deleteOrder(order.getId()))
                    .invoke(response -> {
                        if (response.isSuccessful()) {
                            Navigation.getNavigation().back();
                        }
                    }));
            builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
            builder.create().show();
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(order.getId() != null);
        //todo:
//        binding.orderCardCancel.setOnClickListener(v -> Navigation.getNavigation().callbackBack());

        Navigation.getNavigation().addOnBackListener(backListener);
        requireActivity().setTitle("Заказ");
        binding.tvClient.setText(order.getClient().getName());
        EditTextUtil.setText(binding.tvPhone, order.getPhone());
        EditTextUtil.setText(binding.etComment, order.getComment());
        EditTextUtil.setText(binding.tvAddress, order.getDeliveryAddress());

        binding.tvPrePay.setText(MoneyUtils.moneyWithCurrencyToString(Optional.ofNullable(order.getPrePaymentSum()).orElse(0d)));
        binding.cbDelivery.setChecked(order.isNeedDelivery());
        binding.tvIssue.setText(dateTimeFormatter.format(order.getDeadline()));

        binding.tvCreated.setText(dateTimeFormatter.format(order.getCreateDate()));
        bindSums();
        RecyclerView orderLinesList = view.findViewById(R.id.rvOrderLines);
        orderLinesList.setLayoutManager(new LinearLayoutManager(view.getContext()));

//        if (order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.IN_DELIVERY) {
//            binding.orderCardLineAdd.setVisibility(View.GONE);
//        } else {
//            binding.orderCardLineAdd.setOnClickListener(v -> {
//                OrderLine orderLine = new OrderLine();
//                orderLine.setNum(mAdapter.getOriginalItems().stream().max(Comparator.comparing(OrderLine::getNum))
//                        .map(OrderLine::getNum).map(m -> m + 1).orElse(1));
//                mAdapter.addItem(orderLine);
//                Bundle bundle = new Bundle();
//                bundle.putString("order", GsonHelper.serialize(order));
//                bundle.putInt("orderline", orderLine.getNum());
//                bundle.putString("source", "orderCard");
//                Navigation.getNavigation().goForward(new GoodsFragment(), bundle);
//            });
//        }

        mAdapter = new OrderLinesRecyclerViewAdapter(order);
        if (order.getOrderStatus() == OrderStatus.IN_PROGRESS || order.getOrderStatus() == OrderStatus.DONE) {
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
                bundle.putString("order", GsonHelper.serialize(order));
                bundle.putInt("orderline", orderLine.getNum());
                bundle.putString("source", "orderCard");
                Navigation.getNavigation().goForward(new GoodsFragment(), bundle);
            });
            mAdapter.setChangeSumListener(this::bindSums);

            binding.tvIssue.setOnClickListener(v -> {
                DatePicker datePicker = new DatePicker(requireActivity(),
                        date -> {
                            order.setDeadline(date);
                            binding.tvIssue.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date));
                            binding.tvIssue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                        }, "Выбор даты", "Укажите дату выдачи");
                datePicker.show();
            });

            binding.tvPrePay.setOnClickListener(v -> MoneyPicker.builder(view.getContext())
                    .setTitle("Укажите сумму")
                    .setMinValue(0)
                    .positiveButton(value -> {
                        binding.tvPrePay.setText(String.format("%s", MoneyUtils.moneyWithCurrencyToString(value)));
                        order.setPrePaymentSum(value);
                        bindSums();
                    })
                    .setInitialValue(order.getPrePaymentSum())
                    .build()
                    .show());
            binding.btnShare.setOnClickListener(v -> LinkChannelPicker.builder(v.getContext())
                    .setTitle("Выберите канал связи")
                    .setAction((alertDialog, linkChannel) -> {
                        String phone = String.format("+7%s", binding.tvPhone.getText().toString());
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
            binding.tvAddress.setOnClickListener(v -> v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.field_background)));
            mAdapter.setDoneListener((orderLine, v) -> {
                orderLine.setDone(!orderLine.isDone());
                if (orderLine.isDone()) {
                    v.changeBackgroundTintColor(ContextCompat.getColor(OrderCardFragment.this.requireContext(), R.color.green));
                } else {
                    v.changeBackgroundTintColor();
                }
            });
        } else {
            binding.tvPhone.setInputType(InputType.TYPE_NULL);
            binding.tvAddress.setInputType(InputType.TYPE_NULL);
            binding.etComment.setInputType(InputType.TYPE_NULL);
            binding.cbDelivery.setEnabled(false);
        }
//        if (calcToPay() == 0) {
//            binding.orderCardPaid.setVisibility(View.INVISIBLE);
//        }
//        if (order.getOrderStatus() != OrderStatus.DONE) {
//            binding.orderCardPaid.setVisibility(View.INVISIBLE);
//        } else {
//            binding.orderCardLineAdd.setVisibility(View.INVISIBLE);
//        }
//        binding.orderCardPaid.setOnClickListener(v -> MoneyPicker.builder(requireContext())
//                .setInitialValue(MoneyUtils.stringToDouble(binding.toPay.getText().toString()))
//                .setTitle("Оплата")
//                .setMessage("Подтвердите сумму оплаты")
//                .setMinValue(0)
//                .positiveButton(sum -> {
//                    order.setPaySum(Optional.ofNullable(order.getPaySum()).orElse(0d) + sum);
//                    bindSums();
//                }).build().show());

        binding.saveButton.setOnClickListener(v -> save());

        orderLinesList.setAdapter(mAdapter);
        order.getOrderLines().sort(Comparator.comparing(OrderLine::getNum));
        mAdapter.setItems(order.getOrderLines());
        ColorStateList colorStateList = ColorStateList.valueOf(getBackgroundColor(view));
        binding.btnShare.setBackgroundTintList(colorStateList);
    }

    private int getBackgroundColor(View view) {
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable colorDrawable) {
            return colorDrawable.getColor();
        } else {
            return ContextCompat.getColor(requireContext(), R.color.white);
        }
    }

    private void sendNotification(Order order) {
//        new NetworkExecutorWrapper<>(NetworkService.getInstance().getOrdersApi().setClientIsNotificated(order.getId()))
//                .invokeSync();
//todo:
    }

    private void bindSums() {
        binding.totalSum.setText(MoneyUtils.moneyWithCurrencyToString(calcTotalSum()));
        Double calcToPay = calcToPay();
        binding.tvToPay.setText(MoneyUtils.moneyWithCurrencyToString(calcToPay));
//        binding.paid.setText(MoneyUtils.moneyWithCurrencyToString(order.getPaySum()));
        if (calcToPay.equals(0d)) {
            binding.tvToPay.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
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

    private void save() {
        boolean fail = false;
        fillOrder();
        if (order.getClient() == null) {
            binding.tvClient.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (StringUtils.isEmpty(order.getPhone())
                || order.getPhone().length() != 10) {
            binding.tvPhone.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (order.isNeedDelivery() && StringUtils.isEmpty(order.getDeliveryAddress())) {
            binding.tvAddress.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        mAdapter.getOriginalItems().removeIf(g -> g.getGood() == null);
        if (mAdapter.getOriginalItems().isEmpty()) {
            Toast.makeText(requireContext(), "Невозможно сохранить заказ. Он пуст", Toast.LENGTH_SHORT).show();
            fail = true;
        }
        if (fail) {
            return;
        }
        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getOrdersApi().saveOrder(order)).invoke(response -> {
            incomingOrder = order;
            if (order.getOrderLines().stream().allMatch(OrderLine::isDone)) {
                new NetworkExecutorHelper<>(requireActivity(),
                        NetworkService.getInstance().getOrdersApi().clientIsNotificated(order.getId())).invoke(booleanResponse -> {
                    if (Boolean.FALSE.equals(booleanResponse.body())) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                        builder.setTitle("Заказ полностью выполнен");
                        builder.setMessage("Оповестить клиента?");
                        builder.setPositiveButton("Да", (dialog, id) -> {
                            sendNotification(order);
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

    private void fillOrder() {
        order.setComment(binding.etComment.getText().toString());
        order.setDeliveryAddress(binding.tvAddress.getText().toString());
        order.setPhone(binding.tvPhone.getText().toString());
        order.setNeedDelivery(binding.cbDelivery.isChecked());
        order.getOrderLines().removeIf(r -> r.getGood() == null);
        if (order.getOrderLines().stream().anyMatch(x -> !x.isDone())) {
            order.setOrderStatus(OrderStatus.IN_PROGRESS);
        } else {
            order.setOrderStatus(OrderStatus.DONE);
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Navigation.getNavigation().removeOnBackListener(backListener);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}