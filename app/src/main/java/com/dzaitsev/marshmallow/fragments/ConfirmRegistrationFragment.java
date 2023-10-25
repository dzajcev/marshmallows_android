package com.dzaitsev.marshmallow.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.databinding.FragmentConfirmRegistrationBinding;
import com.dzaitsev.marshmallow.dto.OrdersFilter;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class ConfirmRegistrationFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "confirmRegistrationFragment";
    private FragmentConfirmRegistrationBinding binding;

    private CountDownTimer timer;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentConfirmRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String string = preferences.getString("order-filter", "{}");
        Gson gson = GsonExt.getGson();
        Type type = new TypeToken<OrdersFilter>() {
        }.getType();
        OrdersFilter filter = gson.fromJson(string, type);
        if (filter == null) {
            filter = new OrdersFilter();
        }
        timer = new CountDownTimer(10000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                binding.btnRequest.setText("Через " + millisUntilFinished / 1000 + " сек");
            }

            @Override
            public void onFinish() {
                binding.btnRequest.setText("Запросить");
                binding.btnRequest.setEnabled(true);

            }
        }.start();
        binding.btnCancel.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).goForward(new LoginFragment()));
        binding.btnRequest.setEnabled(false);
        binding.btnRequest.setOnClickListener(v -> {
            binding.btnRequest.setEnabled(false);
            timer.start();
        });
        binding.btnSend.setOnClickListener(v -> {

        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}