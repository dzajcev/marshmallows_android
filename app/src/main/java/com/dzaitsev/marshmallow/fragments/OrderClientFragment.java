package com.dzaitsev.marshmallow.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.databinding.FragmentOrderClientBinding;
import com.dzaitsev.marshmallow.dto.Client;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.DateTimePicker;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderClientFragment extends Fragment {
    private FragmentOrderClientBinding binding;

    private Order order;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
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
        binding.chooseClient.setOnClickListener(v -> {
            bundle.putSerializable("order", order);
            NavHostFragment.findNavController(OrderClientFragment.this)
                    .navigate(R.id.action_orderClientFragment_to_findClientFragment, bundle);
        });
        binding.deadline.setOnClickListener(v -> {
            DateTimePicker dateTimePicker = new DateTimePicker(requireActivity(),
                    date -> {
                        order.setDeadline(date);
                        bind(order);
                    }, "Выбор даты", "Укажите дату выдачи");
            dateTimePicker.show();
        });

        binding.comment.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                EditText e = (EditText) v;
                order.setComment(e.getText().toString());
            }
        });
        binding.delivery.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                EditText e = (EditText) v;
                order.setDeliveryAddress(e.getText().toString());
            }
        });
        binding.prePayment.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                EditText e = (EditText) v;
                order.setPrePaymentSum(MoneyUtils.getInstance().stringToDouble(e.getText().toString()));
            }
        });
        bind(order);
    }

    private void bind(Order order) {
        binding.clientName.setText(Optional.ofNullable(order.getClient()).map(Client::getName).orElse(""));
        binding.phoneNumber.setText(Optional.ofNullable(order.getClient()).map(Client::getPhone).orElse(""));
        binding.delivery.setText(Optional.ofNullable(order.getDeliveryAddress()).orElse(""));
        binding.comment.setText(Optional.ofNullable(order.getComment()).orElse(""));
        Optional.ofNullable(order.getDeadline())
                .ifPresent(o -> {
                    binding.deadline.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(o));
                    binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean save() {
        boolean fail = false;
        if (order.getClient() == null) {
            binding.clientName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (order.getDeadline() == null) {
            binding.deadline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (fail) {
            return false;
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);

        AtomicBoolean result = new AtomicBoolean(true);
        final StringBuffer errMessage = new StringBuffer();
        NetworkService.getInstance()
                .getMarshmallowApi().saveOrder(order).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            result.set(false);
                            errMessage.append(response.message());
                        }
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        result.set(false);
                        errMessage.append(t.getMessage());
                        countDownLatch.countDown();
                    }
                });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!result.get()) {
            new StringUtils.ErrorDialog(requireActivity(), errMessage.toString()).show();
        }
        return result.get();
    }

}