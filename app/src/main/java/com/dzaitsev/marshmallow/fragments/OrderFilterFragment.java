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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.components.DatePicker;
import com.dzaitsev.marshmallow.databinding.FragmentOrderFilterBinding;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.dto.OrdersFilter;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OrderFilterFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "orderFilterFragment";
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private FragmentOrderFilterBinding binding;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentOrderFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Фильтр заказов");
        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String string = preferences.getString("order-filter", "{}");
        Gson gson = GsonExt.getGson();
        Type type = new TypeToken<OrdersFilter>() {
        }.getType();
        OrdersFilter filter = gson.fromJson(string, type);
        if (filter == null) {
            filter = new OrdersFilter();
        }
        binding.orderFilterStart.setText(Optional.ofNullable(filter.getStart()).map(dateTimeFormatter::format).orElse(""));
        binding.orderFilterEnd.setText(Optional.ofNullable(filter.getEnd()).map(dateTimeFormatter::format).orElse(""));
        binding.checkBoxInProgress.setChecked(filter.getStatuses().contains(OrderStatus.IN_PROGRESS));
        binding.checkBoxDone.setChecked(filter.getStatuses().contains(OrderStatus.DONE));
        binding.checkBoxInDelivery.setChecked(filter.getStatuses().contains(OrderStatus.IN_DELIVERY));
        binding.checkBoxShipped.setChecked(filter.getStatuses().contains(OrderStatus.SHIPPED));
        binding.orderFilterCancel.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).back());
        binding.orderFilterStart.setOnClickListener(v -> {
            DatePicker datePicker = new DatePicker(requireActivity(),
                    date -> binding.orderFilterStart.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date)),
                    "Выбор даты", "Укажите дату начала периода");
            datePicker.show();
        });
        binding.orderFilterEnd.setOnClickListener(v -> {
            DatePicker datePicker = new DatePicker(requireActivity(),
                    date -> binding.orderFilterEnd.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date)),
                    "Выбор даты", "Укажите дату окончания периода");
            datePicker.show();
        });
        binding.orderFilterApply.setOnClickListener(v -> {
            SharedPreferences.Editor edit = preferences.edit();
            OrdersFilter ordersFilter = new OrdersFilter();
            ordersFilter.setStart(!StringUtils.isEmpty(binding.orderFilterStart.getText().toString())
                    ? LocalDate.parse(binding.orderFilterStart.getText(), dateTimeFormatter) : null);
            ordersFilter.setEnd(!StringUtils.isEmpty(binding.orderFilterEnd.getText().toString())
                    ? LocalDate.parse(binding.orderFilterEnd.getText(), dateTimeFormatter) : null);
            if (binding.checkBoxInProgress.isChecked()) {
                ordersFilter.getStatuses().add(OrderStatus.IN_PROGRESS);
            }
            if (binding.checkBoxDone.isChecked()) {
                ordersFilter.getStatuses().add(OrderStatus.DONE);
            }
            if (binding.checkBoxInDelivery.isChecked()) {
                ordersFilter.getStatuses().add(OrderStatus.IN_DELIVERY);
            }
            if (binding.checkBoxShipped.isChecked()) {
                ordersFilter.getStatuses().add(OrderStatus.SHIPPED);
            }
            edit.putString("order-filter", GsonExt.getGson().toJson(ordersFilter));
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