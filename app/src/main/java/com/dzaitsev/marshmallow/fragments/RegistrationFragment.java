package com.dzaitsev.marshmallow.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.databinding.FragmentRegistrationBinding;
import com.dzaitsev.marshmallow.dto.OrdersFilter;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


public class RegistrationFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "registrationFragment";
    private FragmentRegistrationBinding binding;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
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
        binding.btnBack.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).back());
        binding.btnRegistration.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).goForward(new ConfirmRegistrationFragment()));
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