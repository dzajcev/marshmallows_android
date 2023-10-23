package com.dzaitsev.marshmallow.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.components.DatePicker;
import com.dzaitsev.marshmallow.databinding.FragmentDeliveryFilterBinding;
import com.dzaitsev.marshmallow.dto.DeliveryFilter;
import com.dzaitsev.marshmallow.dto.DeliveryStatus;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DeliveryFilterFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "deliveryFilterFragment";
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private FragmentDeliveryFilterBinding binding;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentDeliveryFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Фильтр доставок");
        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String string = preferences.getString("delivery-filter", "{}");
        Gson gson = GsonExt.getGson();
        DeliveryFilter filter = gson.fromJson(string, DeliveryFilter.class);
        if (filter == null) {
            filter = new DeliveryFilter();
        }
        binding.deliveryFilterStart.setText(Optional.ofNullable(filter.getStart()).map(dateTimeFormatter::format).orElse(""));
        binding.deliveryFilterEnd.setText(Optional.ofNullable(filter.getEnd()).map(dateTimeFormatter::format).orElse(""));
        binding.checkBoxInProgress.setChecked(filter.getStatuses().contains(DeliveryStatus.IN_PROGRESS));
        binding.checkBoxDone.setChecked(filter.getStatuses().contains(DeliveryStatus.DONE));
        binding.checkBoxNew.setChecked(filter.getStatuses().contains(DeliveryStatus.NEW));
        binding.deliveryFilterCancel.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).back());
        binding.deliveryFilterStart.setOnClickListener(v -> {
            DatePicker datePicker = new DatePicker(requireActivity(),
                    date -> binding.deliveryFilterStart.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date)),
                    "Выбор даты", "Укажите дату начала периода");
            datePicker.show();
        });
        binding.deliveryFilterEnd.setOnClickListener(v -> {
            DatePicker datePicker = new DatePicker(requireActivity(),
                    date -> binding.deliveryFilterEnd.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date)),
                    "Выбор даты", "Укажите дату окончания периода");
            datePicker.show();
        });
        binding.deliveryFilterApply.setOnClickListener(v -> {
            SharedPreferences.Editor edit = preferences.edit();
            DeliveryFilter deliveryFilter = new DeliveryFilter();
            deliveryFilter.setStart(!StringUtils.isEmpty(binding.deliveryFilterStart.getText().toString())
                    ? LocalDate.parse(binding.deliveryFilterStart.getText(), dateTimeFormatter) : null);
            deliveryFilter.setEnd(!StringUtils.isEmpty(binding.deliveryFilterEnd.getText().toString())
                    ? LocalDate.parse(binding.deliveryFilterEnd.getText(), dateTimeFormatter) : null);
            if (binding.checkBoxInProgress.isChecked()) {
                deliveryFilter.getStatuses().add(DeliveryStatus.IN_PROGRESS);
            }
            if (binding.checkBoxDone.isChecked()) {
                deliveryFilter.getStatuses().add(DeliveryStatus.DONE);
            }
            if (binding.checkBoxNew.isChecked()) {
                deliveryFilter.getStatuses().add(DeliveryStatus.NEW);
            }
            edit.putString("delivery-filter", GsonExt.getGson().toJson(deliveryFilter));
            edit.apply();
            Navigation.getNavigation(requireActivity()).back();
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