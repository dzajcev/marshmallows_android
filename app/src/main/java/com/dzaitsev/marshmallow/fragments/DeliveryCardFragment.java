package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.DeliveryOrderRecyclerViewAdapter;
import com.dzaitsev.marshmallow.components.DatePicker;
import com.dzaitsev.marshmallow.components.TimePicker;
import com.dzaitsev.marshmallow.databinding.FragmentDeliveryCardBinding;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.User;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class DeliveryCardFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "deliveryCardFragment";
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private FragmentDeliveryCardBinding binding;
    private DeliveryOrderRecyclerViewAdapter mAdapter;

//    private final OrderSelectorFragment orderSelectorFragment = new OrderSelectorFragment();
//    private final DeliveryExecutorFragment deliveryExecutorFragment = new DeliveryExecutorFragment(false);
    private final Navigation.OnBackListener backListener = fragment -> {
        if (DeliveryCardFragment.IDENTITY.equals(fragment.identity())) {
            if (DeliveryCardFragment.this.hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryCardFragment.this.getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> DeliveryCardFragment.this.save());
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> Navigation.getNavigation().back());
                builder.create().show();
            } else {
                Navigation.getNavigation().back();
            }
        }
        return false;
    };

    private Delivery incomingDelivery;
    private Delivery delivery;

    private boolean hasChanges() {
        return !delivery.equals(incomingDelivery);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        MenuItem deleteOrder = menu.add("Удалить");
        deleteOrder.setOnMenuItemClickListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Вы уверены?");
            builder.setPositiveButton("Да", (dialog, id) -> new NetworkExecutorHelper<>(requireActivity(),
                    NetworkService.getInstance().getDeliveryApi().deleteDelivery(delivery.getId()))
                    .invoke(response -> {
                        if (response.isSuccessful()) {
                            Navigation.getNavigation().back();
                        }
                    }));
            builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
            builder.create().show();
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        delivery = Objects.requireNonNull(GsonHelper.deserialize(requireArguments().getString("delivery"), Delivery.class));
        setHasOptionsMenu(delivery.getId() != null && delivery.isMy());
        if (delivery.getId() != null) {
            new NetworkExecutorHelper<>(requireActivity(), NetworkService.getInstance().getDeliveryApi().getDelivery(delivery.getId()))
                    .invoke(deliveryResponse -> {
                        if (deliveryResponse.isSuccessful()) {
                            incomingDelivery = deliveryResponse.body();
                        }
                    });
        }

        binding = FragmentDeliveryCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Карточка доставки");
        binding.deliveryCardCancel.setOnClickListener(v -> Navigation.getNavigation().callbackBack());
        Navigation.getNavigation().addOnBackListener(backListener);
        mAdapter = new DeliveryOrderRecyclerViewAdapter();
        binding.deliveryCardOrders.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.deliveryCardOrders.setAdapter(mAdapter);
        binding.deliveryCardAddOrders.setOnClickListener(v -> {
            Bundle orders = new Bundle();
            orders.putString("delivery", GsonHelper.serialize(delivery));
            Navigation.getNavigation().forward(OrderSelectorFragment.IDENTITY, orders);
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
                    binding.deliveryCardStart.setText(Optional.ofNullable(d.getStart()).map(timeFormatter::format).orElse(""));
                    binding.deliveryCardEnd.setText(Optional.ofNullable(d.getEnd()).map(timeFormatter::format).orElse(""));
                    binding.deliveryCardDateDelivery.setText(Optional.ofNullable(d.getDeliveryDate()).map(dateTimeFormatter::format).orElse(""));
                    mAdapter.setItems(d.getOrders());
                });
        binding.deliveryCardSave.setOnClickListener(v -> save());
        if (delivery.isMy()) {
            binding.txtDeliveryCardExecutor.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("delivery", GsonHelper.serialize(delivery));
                Navigation.getNavigation().forward(DeliveryExecutorFragment.IDENTITY, bundle);
            });
            mAdapter.setDeleteItemListener(item -> {
                delivery.getOrders().remove(item);
                binding.deliveryCardOrders.setAdapter(mAdapter);
                mAdapter.setItems(delivery.getOrders());
            });
        }
        mAdapter.setSaveOrderListener(item -> new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getOrdersApi().saveOrder(item)).
                invoke(response -> {
                }));
        if (delivery.getExecutor() == null) {
            delivery.setExecutor(AuthorizationHelper.getInstance().getUserData().orElse(null));
        }
        binding.txtDeliveryCardExecutor.setText(Optional.ofNullable(delivery.getExecutor())
                .map(f -> {
                    User user = AuthorizationHelper.getInstance().getUserData().orElse(null);
                    if (user != null && f.getId().equals(user.getId())) {
                        return "Я";
                    } else {
                        return f.getFullName();
                    }
                }).orElse(""));
    }
    private void save() {
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
            return;
        }
        if (delivery.getStart().isAfter(delivery.getEnd())) {
            binding.deliveryCardEnd.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            binding.deliveryCardStart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            Toast.makeText(requireContext(), "Время начала позже времени окончания", Toast.LENGTH_SHORT).show();
            return;
        }
        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getDeliveryApi().saveDelivery(delivery)).
                invoke(response -> {
                    incomingDelivery = delivery;
                    Navigation.getNavigation().back();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
        binding = null;
        Navigation.getNavigation().removeOnBackListener(backListener);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}