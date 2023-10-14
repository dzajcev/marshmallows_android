package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.dzaitsev.marshmallow.MainActivity;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.databinding.FragmentOrderCardBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderCardFragment extends Fragment {

    private FragmentOrderCardBinding binding;


    private Order incomingOrder;
    private EditText goodName;
    private EditText goodPrice;

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
        Order order = constructOrder();
//        if ((order.getName() == null || order.getName().isEmpty()) && order.getPrice() == null) {
//            return false;
//        }
        return !order.equals(incomingOrder);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        FragmentActivity fragmentActivity = requireActivity();
//        if (fragmentActivity instanceof MainActivity) {
//            MainActivity ma = (MainActivity) fragmentActivity;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentOrderCardBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private View.OnKeyListener keyListener = (v, keyCode, event) -> {
        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.field_background));
        return false;
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//
//        Good good = requireArguments().getSerializable("good", Good.class);
//        incomingGood = Objects.requireNonNull(good).clone();
//        goodName = view.findViewById(R.id.goodCardName);
//        goodName.setOnKeyListener(keyListener);
//        goodName.setText(good.getName());
//        goodPrice = view.findViewById(R.id.goodCardPrice);
//        goodPrice.setOnKeyListener(keyListener);
//        goodPrice.setText(Optional.ofNullable(good.getPrice()).map(formatter::format).orElse(""));
//        ImageButton cancel = view.findViewById(R.id.goodCardCancel);
//        cancel.setOnClickListener(v -> requireActivity().onBackPressed());
//        ImageButton save = view.findViewById(R.id.goodCardSave);
//        save.setOnClickListener(v -> {
//            if (save()) {
//                requireActivity().onBackPressed();
//            }
//        });

    }

    private boolean save() {
        boolean fail = false;
        if (goodName.getText() == null || goodName.getText().toString().isEmpty()) {
            goodName.setBackgroundColor(Color.parseColor("#fa8c8c"));
            fail = true;
        }
        if (goodPrice.getText() == null || goodPrice.getText().toString().isEmpty()) {
            goodPrice.setBackgroundColor(Color.parseColor("#fa8c8c"));
            fail = true;
        }
        if (fail) {
            return false;
        }
        Order order = constructOrder();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        AtomicBoolean result = new AtomicBoolean(true);
        final StringBuffer errMessage = new StringBuffer();
        NetworkService.getInstance()
                .getMarshmallowApi().saveOrder(order).enqueue(new Callback<Void>() {
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
        incomingOrder = order;
        if (!result.get()) {
            new StringUtils.ErrorDialog(requireActivity(), errMessage.toString()).show();
        }
        return result.get();
    }

    private Order constructOrder() {
        Order good = new Order();
//        good.setId(incomingOrder == null ? null : incomingOrder.getId());
//        good.setName(goodName.getText().toString());
//        try {
//            if (goodPrice.getText() != null && !goodPrice.getText().toString().isEmpty()) {
//                good.setPrice(Optional.ofNullable(formatter.parse(goodPrice.getText().toString())).orElse(-1d).doubleValue());
//            }
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }
        return good;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        FragmentActivity fragmentActivity = requireActivity();
        if (fragmentActivity instanceof MainActivity) {
            MainActivity ma = (MainActivity) fragmentActivity;
            ma.setNavigationBackListener(null);
        }
    }

}