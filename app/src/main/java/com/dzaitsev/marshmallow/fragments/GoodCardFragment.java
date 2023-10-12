package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.dzaitsev.marshmallow.ErrorDialog;
import com.dzaitsev.marshmallow.MainActivity;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.databinding.FragmentGoodCardBinding;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.service.NetworkService;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoodCardFragment extends Fragment {

    private FragmentGoodCardBinding binding;


    private Good incomingGood;
    private EditText goodName;
    private EditText goodPrice;
    private final NumberFormat formatter = new DecimalFormat("#0.00");

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
        incomingGood = Objects.requireNonNull(good).clone();
        goodName = view.findViewById(R.id.goodCardName);
        goodName.setOnKeyListener(keyListener);
        goodName.setText(good.getName());
        goodPrice = view.findViewById(R.id.goodCardPrice);
        goodPrice.setOnKeyListener(keyListener);
        goodPrice.setText(Optional.ofNullable(good.getPrice()).map(formatter::format).orElse(""));
        ImageButton cancel = view.findViewById(R.id.goodCardCancel);
        cancel.setOnClickListener(v -> requireActivity().onBackPressed());
        ImageButton save = view.findViewById(R.id.goodCardSave);
        save.setOnClickListener(v -> {
            if (save()) {
                requireActivity().onBackPressed();
            }
        });

    }

    private boolean save() {
        boolean fail = false;
        if (goodName.getText() == null || goodName.getText().toString().isEmpty()) {
            goodName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (goodPrice.getText() == null || goodPrice.getText().toString().isEmpty()) {
            goodPrice.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (fail) {
            return false;
        }
        Good good = constructGood();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        AtomicBoolean result = new AtomicBoolean(true);
        final StringBuffer errMessage = new StringBuffer();
        NetworkService.getInstance()
                .getMarshmallowApi().saveGood(good).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!response.isSuccessful()) {
                            result.set(false);
                            errMessage.append(response.message());
                        }
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
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
        incomingGood = good;
        if (!result.get()) {
            new ErrorDialog(requireActivity(), errMessage.toString()).show();
        }
        return result.get();
    }

    private Good constructGood() {
        Good good = new Good();
        good.setId(incomingGood == null ? null : incomingGood.getId());
        good.setName(goodName.getText().toString());
        try {
            if (goodPrice.getText() != null && !goodPrice.getText().toString().isEmpty()) {
                good.setPrice(Optional.ofNullable(formatter.parse(goodPrice.getText().toString())).orElse(-1d).doubleValue());
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
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

