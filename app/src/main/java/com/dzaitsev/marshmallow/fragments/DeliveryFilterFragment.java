package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.components.DatePicker;
import com.dzaitsev.marshmallow.databinding.FragmentDeliveryFilterBinding;
import com.dzaitsev.marshmallow.dto.DeliveryFilter;
import com.dzaitsev.marshmallow.dto.DeliveryStatus;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.orderfilter.FiltersHelper;

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

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Фильтр доставок");
        FiltersHelper.getInstance().getDeliveryFilter()
                .ifPresent(filter -> {
                    binding.deliveryFilterStart.setText(Optional.ofNullable(filter.getStart()).map(dateTimeFormatter::format).orElse(""));
                    binding.deliveryFilterEnd.setText(Optional.ofNullable(filter.getEnd()).map(dateTimeFormatter::format).orElse(""));
                    binding.checkBoxInProgress.setChecked(filter.getStatuses().contains(DeliveryStatus.IN_PROGRESS));
                    binding.checkBoxDone.setChecked(filter.getStatuses().contains(DeliveryStatus.DONE));
                    binding.checkBoxNew.setChecked(filter.getStatuses().contains(DeliveryStatus.NEW));
                });

        binding.deliveryFilterCancel.setOnClickListener(v -> Navigation.getNavigation().back());
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
            FiltersHelper.getInstance().updateDeliveryFilter(deliveryFilter);
            Navigation.getNavigation().back();
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