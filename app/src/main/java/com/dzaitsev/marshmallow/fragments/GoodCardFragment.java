package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.MainActivity;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.PriceHistoryRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentGoodCardBinding;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.service.NetworkExecutorCallback;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class GoodCardFragment extends Fragment {

    private FragmentGoodCardBinding binding;
    private Good incomingGood;

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
        Good good = constructGood();
        if ((good.getName() == null || good.getName().isEmpty()) && good.getPrice() == null) {
            return false;
        }
        return !good.equals(incomingGood);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity fragmentActivity = requireActivity();
        if (fragmentActivity instanceof MainActivity ma) {
            ma.setNavigationBackListener(() -> {
                if (hasChanges()) {
                    requireActivity().onBackPressed();
                    return false;
                } else {
                    return true;
                }
            });
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentGoodCardBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private final View.OnKeyListener keyListener = (v, keyCode, event) -> {
        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.field_background));
        return false;
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Good good = requireArguments().getSerializable("good", Good.class);
        if (good == null || good.getPrices().isEmpty()) {
            binding.goodsCardPriceHistoryLabel.setVisibility(View.GONE);
        }
        incomingGood = Objects.requireNonNull(good).clone();
        binding.goodCardName.setOnKeyListener(keyListener);
        binding.goodCardName.setText(good.getName());
        binding.goodCardPrice.setOnKeyListener(keyListener);

        binding.goodCardPrice.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(good.getPrice()));
        binding.goodCardPrice.setOnFocusChangeListener((view1, b) -> {
            if (b) {
                binding.goodCardPrice.setText(MoneyUtils.getInstance()
                        .moneyToString(MoneyUtils.getInstance()
                                .stringToDouble(binding.goodCardPrice.getText().toString())));
            } else {
                binding.goodCardPrice.setText(MoneyUtils.getInstance()
                        .moneyWithCurrencyToString(MoneyUtils.getInstance()
                                .stringToDouble(binding.goodCardPrice.getText().toString())));
            }
        });
        binding.goodCardDescription.setText(good.getDescription());
        ImageButton cancel = view.findViewById(R.id.goodCardCancel);
        cancel.setOnClickListener(v -> requireActivity().onBackPressed());
        ImageButton save = view.findViewById(R.id.goodCardSave);
        save.setOnClickListener(v -> {
            if (save()) {
                requireActivity().onBackPressed();
            }
        });
        binding.goodCardPriceHistoryList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        PriceHistoryRecyclerViewAdapter priceHistoryRecyclerViewAdapter = new PriceHistoryRecyclerViewAdapter();
        binding.goodCardPriceHistoryList.setAdapter(priceHistoryRecyclerViewAdapter);

        priceHistoryRecyclerViewAdapter.setItems(good.getPrices().stream()
                .sorted((price, t1) -> t1.getCreateDate().compareTo(price.getCreateDate())).collect(Collectors.toList()));
    }

    private boolean save() {
        boolean fail = false;
        if (binding.goodCardName.getText() == null || binding.goodCardName.getText().toString().isEmpty()) {
            binding.goodCardName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (binding.goodCardPrice.getText() == null || binding.goodCardPrice.getText().toString().isEmpty()) {
            binding.goodCardPrice.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (fail) {
            return false;
        }
        Good good = constructGood();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        NetworkExecutorCallback<Void> callback = new NetworkExecutorCallback<>(requireActivity(),
                response -> countDownLatch.countDown(), countDownLatch);
        NetworkService.getInstance().getMarshmallowApi().saveGood(good)
                .enqueue(callback);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        incomingGood = good;
        return callback.isSuccess();
    }

    private Good constructGood() {
        Good good = new Good();
        good.setId(incomingGood == null ? null : incomingGood.getId());
        good.setName(binding.goodCardName.getText().toString());
        good.setDescription(binding.goodCardDescription.getText().toString());
        good.setPrice(MoneyUtils.getInstance().stringToDouble(binding.goodCardPrice.getText().toString()));
        return good;
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

