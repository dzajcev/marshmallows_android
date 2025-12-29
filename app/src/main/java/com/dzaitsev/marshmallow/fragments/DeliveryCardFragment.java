package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.DeliveryOrderRecyclerViewAdapter;
import com.dzaitsev.marshmallow.components.DatePicker;
import com.dzaitsev.marshmallow.databinding.FragmentDeliveryCardBinding;
import com.dzaitsev.marshmallow.dto.Delivery;
import com.dzaitsev.marshmallow.dto.DeliveryStatus;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.dto.User;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import retrofit2.Response;

public class DeliveryCardFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "deliveryCardFragment";
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private FragmentDeliveryCardBinding binding;
    private DeliveryOrderRecyclerViewAdapter mAdapter;

    private final Navigation.OnBackListener backListener = fragment -> {
        if (DeliveryCardFragment.IDENTITY.equals(fragment.identity())) {
            if (DeliveryCardFragment.this.hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryCardFragment.this.getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> DeliveryCardFragment.this.save(true));
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
                        binding.layoutDate.setError(null); // Сброс ошибки
                    }, "Выбор даты", "Укажите дату доставки");
            datePicker.show();
        });

        binding.deliveryCardStart.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(requireActivity());
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(23);
            Optional.ofNullable(delivery.getStart()).ifPresent(d -> numberPicker.setValue(d.getHour()));
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("Выбор времени");
            builder.setMessage("Укажите время начала доставки");
            builder.setView(numberPicker);
            builder.setPositiveButton("OK", (dialog, which) -> {
                LocalTime time = LocalTime.of(numberPicker.getValue(), 0);
                delivery.setStart(time);
                binding.deliveryCardStart.setText(timeFormatter.format(time));
                binding.layoutStart.setError(null); // Сброс ошибки
            });
            builder.setNegativeButton("Отмена", null);
            builder.create().show();
        });

        binding.deliveryCardEnd.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(requireActivity());
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(23);
            Optional.ofNullable(delivery.getEnd()).ifPresent(d -> numberPicker.setValue(d.getHour()));
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("Выбор времени");
            builder.setMessage("Укажите время окончания доставки");
            builder.setView(numberPicker);
            builder.setPositiveButton("OK", (dialog, which) -> {
                LocalTime time = LocalTime.of(numberPicker.getValue(), 0);
                delivery.setEnd(time);
                binding.deliveryCardEnd.setText(timeFormatter.format(time));
                binding.layoutEnd.setError(null); // Сброс ошибки
            });
            builder.setNegativeButton("Отмена", null);
            builder.create().show();
        });

        binding.deliveryParametersHeader.setOnClickListener(v -> {
            if (binding.deliveryParametersContent.getVisibility() == View.VISIBLE) {
                binding.deliveryParametersContent.setVisibility(View.GONE);
                binding.expandIcon.setImageResource(R.drawable.ic_expand_more_24);
            } else {
                binding.deliveryParametersContent.setVisibility(View.VISIBLE);
                binding.expandIcon.setImageResource(R.drawable.ic_expand_less_24);
            }
        });

        binding.deliveryCardComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                delivery.setComment(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Optional.ofNullable(delivery)
                .ifPresent(d -> {
                    binding.deliveryCardStart.setText(Optional.ofNullable(d.getStart()).map(timeFormatter::format).orElse(""));
                    binding.deliveryCardEnd.setText(Optional.ofNullable(d.getEnd()).map(timeFormatter::format).orElse(""));
                    binding.deliveryCardDateDelivery.setText(Optional.ofNullable(d.getDeliveryDate()).map(dateTimeFormatter::format).orElse(""));
                    binding.deliveryCardComment.setText(d.getComment());
                    mAdapter.setItems(d.getOrders());
                });

        binding.deliveryCardSave.setOnClickListener(v -> save(true));
        if (delivery.isMy()) {
            binding.txtDeliveryCardExecutor.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("delivery", GsonHelper.serialize(delivery));
                Navigation.getNavigation().forward(DeliveryExecutorFragment.IDENTITY, bundle);
            });
            mAdapter.setDeleteItemListener(item -> {
                new NetworkExecutorHelper<>(requireActivity(),
                        NetworkService.getInstance().getDeliveryApi().deleteDeliveryOrder(delivery.getId(), item.getId()))
                        .invoke(new Consumer<Response<Void>>() {
                            @Override
                            public void accept(Response<Void> voidResponse) {
                            }
                        });
                delivery.getOrders().remove(item);
                binding.deliveryCardOrders.setAdapter(mAdapter);
                mAdapter.setItems(delivery.getOrders());
            });
        }

        mAdapter.setSaveOrderListener(item -> new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getOrdersApi().saveOrder(item)).
                invoke(response -> {
                    if (response.isSuccessful()) {
                        Set<OrderStatus> statuses = delivery.getOrders()
                                .stream()
                                .map(Order::getOrderStatus)
                                .collect(Collectors.toSet());
                        if (statuses.size() > 1) {
                            delivery.setDeliveryStatus(DeliveryStatus.IN_PROGRESS);
                        } else {
                            OrderStatus status = statuses.iterator().next();
                            if (status == OrderStatus.IN_DELIVERY) {
                                delivery.setDeliveryStatus(DeliveryStatus.NEW);
                            } else if (status == OrderStatus.SHIPPED) {
                                delivery.setDeliveryStatus(DeliveryStatus.DONE);
                            }
                        }
                        save(false);
                    }
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

    private boolean validateData() {
        // Сброс предыдущих ошибок
        binding.layoutDate.setError(null);
        binding.layoutStart.setError(null);
        binding.layoutEnd.setError(null);

        boolean isValid = true;
        if (delivery.getDeliveryDate() == null) {
            binding.layoutDate.setError("Укажите дату");
            isValid = false;
        }
        if (delivery.getStart() == null) {
            binding.layoutStart.setError("Укажите время");
            isValid = false;
        }
        if (delivery.getEnd() == null) {
            binding.layoutEnd.setError("Укажите время");
            isValid = false;
        }

        if (!isValid) {
            return false; // Нет смысла проверять дальше, если базовые поля не заполнены
        }

        if (delivery.getStart().isAfter(delivery.getEnd())) {
            binding.layoutStart.setError("Начало не может быть позже окончания");
            binding.layoutEnd.setError("Время окончания не может быть раньше времени начала");
            isValid = false;
        }

        if (mAdapter.getOriginalItems().isEmpty()) {
            Toast.makeText(requireContext(), "Невозможно сохранить доставку. Она пуста", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void save(boolean withBack) {
        if (!validateData()) {
            return;
        }

        new NetworkExecutorHelper<>(requireActivity(),
                NetworkService.getInstance().getDeliveryApi().saveDelivery(delivery)).
                invoke(response -> {
                    if (response.isSuccessful()) {
                        incomingDelivery = delivery;
                        if (withBack) {
                            Navigation.getNavigation().back();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    }
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
