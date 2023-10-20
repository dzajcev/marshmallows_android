package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.DeliveryOrderRecyclerViewAdapter;
import com.dzaitsev.marshmallow.components.DatePicker;
import com.dzaitsev.marshmallow.components.TimePicker;
import com.dzaitsev.marshmallow.databinding.FragmentDeliveryCardBinding;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.response.DeliveryResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DeliveryCardFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "deliveryCardFragment";
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private FragmentDeliveryCardBinding binding;
    private DeliveryOrderRecyclerViewAdapter mAdapter;

    private final OrderSelectorFragment orderSelectorFragment = new OrderSelectorFragment();
    private final Navigation.OnBackListener backListener = fragment -> {
        if (DeliveryCardFragment.this == fragment) {
            if (DeliveryCardFragment.this.hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryCardFragment.this.getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> {
                    if (DeliveryCardFragment.this.save()) {
                        Navigation.getNavigation(DeliveryCardFragment.this.requireActivity()).back();
                    }
                });
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> Navigation.getNavigation(DeliveryCardFragment.this.requireActivity()).back());
                builder.create().show();
            } else {
                Navigation.getNavigation(DeliveryCardFragment.this.requireActivity()).back();
            }
        }
        return false;
    };

    private Delivery incomingDelivery;
    private Delivery delivery;

    private boolean hasChanges() {
        return !delivery.equals(incomingDelivery);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        delivery = requireArguments().getSerializable("delivery", Delivery.class);
        incomingDelivery = delivery.clone();
        binding = FragmentDeliveryCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Карточка доставки");
        binding.deliveryCardCancel.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).callbackBack());
        if (delivery.getId() != null) {
            NetworkExecutor<DeliveryResponse> orderResponseNetworkExecutor = new NetworkExecutor<>(requireActivity(),
                    NetworkService.getInstance().getMarshmallowApi().getDelivery(delivery.getId()),
                    response -> Optional.ofNullable(response.body())
                            .ifPresent(deliveryResponse -> {
                                if (deliveryResponse.getDeliveries() != null && !deliveryResponse.getDeliveries().isEmpty()) {
                                    incomingDelivery = deliveryResponse.getDeliveries().iterator().next();
                                }

                            }), true);
            orderResponseNetworkExecutor.invoke();
            if (!orderResponseNetworkExecutor.isSuccess()) {
                Navigation.getNavigation(requireActivity()).removeOnBackListener(backListener);
                return;
            }
        } else {
            incomingDelivery = new Delivery();
        }
        Navigation.getNavigation(requireActivity()).addOnBackListener(backListener);
        mAdapter = new DeliveryOrderRecyclerViewAdapter();
        binding.deliveryCardOrders.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.deliveryCardOrders.setAdapter(mAdapter);
        binding.deliveryCardAddOrders.setOnClickListener(v -> {
            Bundle orders = new Bundle();
            orders.putSerializable("delivery", delivery);
            Navigation.getNavigation(requireActivity()).goForward(orderSelectorFragment, orders);
        });
        binding.deliveryCardDateDelivery.setOnClickListener(v -> {
            DatePicker datePicker = new DatePicker(requireActivity(),
                    date -> {
                        delivery.setDeliveryDate(date);
                        binding.deliveryCardDateDelivery.setText(dateTimeFormatter.format(date));
                        binding.deliveryCardDateDelivery.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                    }, "Выбор даты", "Укажите дату доставки");
            datePicker.show();
        });
        binding.deliveryCardStart.setOnClickListener(v -> {
            TimePicker datePicker = new TimePicker(requireActivity(),
                    time -> {
                        delivery.setStart(time);
                        binding.deliveryCardStart.setText(timeFormatter.format(time));
                        binding.deliveryCardStart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                    }, "Выбор времени", "Укажите время начала доставки");
            datePicker.show();
        });
        binding.deliveryCardEnd.setOnClickListener(v -> {
            TimePicker datePicker = new TimePicker(requireActivity(),
                    time -> {
                        delivery.setEnd(time);
                        binding.deliveryCardEnd.setText(timeFormatter.format(time));
                        binding.deliveryCardEnd.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                    }, "Выбор времени", "Укажите время окончания доставки");
            datePicker.show();
        });
        Optional.ofNullable(delivery)
                .ifPresent(d -> {
                    binding.deliveryCardStart.setText(timeFormatter.format(d.getStart()));
                    binding.deliveryCardEnd.setText(timeFormatter.format(d.getEnd()));
                    binding.deliveryCardDateDelivery.setText(dateTimeFormatter.format(d.getDeliveryDate()));
                    mAdapter.setItems(d.getOrders());
                    binding.deliveryCardStart.requestLayout();
                    binding.deliveryCardEnd.requestLayout();
                });
        binding.deliveryCardSave.setOnClickListener(v -> {
            save();
            Navigation.getNavigation(requireActivity()).back();
        });
        mAdapter.setDeleteItemListener(item -> {
            delivery.getOrders().remove(item);
            binding.deliveryCardOrders.setAdapter(mAdapter);
            mAdapter.setItems(delivery.getOrders());
        });
        view.post(() -> {
            ColorStateList colorStateList = ColorStateList.valueOf(getBackgroundColor(view));
            binding.deliveryCardFinishDelivery.setBackgroundTintList(colorStateList);
        });
        binding.deliveryCardFinishDelivery.setOnClickListener(v -> {
            boolean toShip = delivery.getOrders().stream().anyMatch(a -> !a.isShipped());
            AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryCardFragment.this.getActivity());
            builder.setTitle("Вы уверены, что хотите отметить все заказы " +
                    (toShip ? "" : "не") +
                    " доставленными?");
            builder.setPositiveButton("Да", (dialog, id) -> {
                delivery.getOrders().forEach(o -> mAdapter.setShipped(toShip, o));
            });
            builder.setNegativeButton("Нет", (dialog, id) -> {
            });
            builder.create().show();
        });
    }

    private int getBackgroundColor(View view) {
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable colorDrawable) {
            return colorDrawable.getColor();
        } else {
            return ContextCompat.getColor(requireContext(), R.color.white);
        }
    }

    private boolean save() {
        boolean fail = false;
        if (delivery.getDeliveryDate() == null) {
            binding.deliveryCardDateDelivery.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (delivery.getStart() == null) {
            binding.deliveryCardStart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (delivery.getEnd() == null) {
            binding.deliveryCardEnd.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (mAdapter.getOriginalItems().isEmpty()) {
            Toast.makeText(requireContext(), "Невозможно сохранить доставку. Она пуста", Toast.LENGTH_SHORT).show();
            fail = true;
        }
        if (fail) {
            return false;
        }
        NetworkExecutor<Void> callback = new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getMarshmallowApi().saveDelivery(delivery), response -> {
        }, true);
        callback.invoke();
        incomingDelivery = delivery;
        return callback.isSuccess();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
        binding = null;
        Navigation.getNavigation(requireActivity()).removeOnBackListener(backListener);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}