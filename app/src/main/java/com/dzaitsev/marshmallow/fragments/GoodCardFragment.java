package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.PriceHistoryRecyclerViewAdapter;
import com.dzaitsev.marshmallow.components.MoneyPicker;
import com.dzaitsev.marshmallow.databinding.FragmentGoodCardBinding;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;
import com.dzaitsev.marshmallow.dto.response.OrderResponse;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.service.api.GoodsApi;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import retrofit2.Response;

public class GoodCardFragment extends Fragment implements IdentityFragment {

    public static final String IDENTITY = "goodCardFragment";

    private FragmentGoodCardBinding binding;
    private Good incomingGood;

    private Good good;

    private final Navigation.OnBackListener backListener = fragment -> {
        if (GoodCardFragment.this == fragment) {
            if (GoodCardFragment.this.hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GoodCardFragment.this.getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> {
                    if (GoodCardFragment.this.save()) {
                        Navigation.getNavigation(GoodCardFragment.this.requireActivity()).back();
                    }
                });
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> Navigation.getNavigation(GoodCardFragment.this.requireActivity()).back());
                builder.create().show();
            } else {
                Navigation.getNavigation(GoodCardFragment.this.requireActivity()).back();
            }
        }
        return false;
    };

    private boolean hasChanges() {
        fillGood();
        if (StringUtils.isEmpty(good.getName()) && good.getPrice() == null) {
            return false;
        }
        return !good.equals(incomingGood);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem deleteClient = menu.add("Удалить");
        if (incomingGood.isActive()) {
            deleteClient.setTitle("Удалить");
        } else {
            deleteClient.setTitle("Восстановить");
        }
        deleteClient.setOnMenuItemClickListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(GoodCardFragment.this.getActivity());
            builder.setTitle((incomingGood.isActive() ? "Удаление" : "Восстановление") + " зефирки?");
            Boolean hasOrders = null;

            if (incomingGood.isActive()) {
                hasOrders = checkGoodOnOrders();
                if (hasOrders == null) {
                    return false;
                }
            }
            String text = "";
            if (incomingGood.isActive() && Boolean.FALSE.equals(hasOrders)) {
                text = "\nЗапись будет удалена безвозвратно";
            }
            builder.setMessage("Вы уверены?" + text);
            builder.setPositiveButton("Да", (dialog, id) -> {
                GoodsApi goodsApi = NetworkService.getInstance().getGoodsApi();
                if (new NetworkExecutor<>(requireActivity(),
                        incomingGood.isActive() ? goodsApi.deleteGood(good.getId()) : goodsApi.restoreGood(good.getId()))
                        .invokeSync().isSuccessful()) {
                    Navigation.getNavigation(getActivity()).back();
                }
            });
            builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
            builder.create().show();
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        good = requireArguments().getSerializable("good", Good.class);
        incomingGood = Objects.requireNonNull(good).clone();
        setHasOptionsMenu(good.getId() != null);
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
        requireActivity().setTitle("Карточка зефирки");
        binding.goodCardCancel.setOnClickListener(v -> Navigation.getNavigation(requireActivity()).callbackBack());
        if (good.getId() != null) {
            Response<GoodsResponse> goodsResponse = new NetworkExecutor<>(requireActivity(),
                    NetworkService.getInstance().getGoodsApi().getGood(good.getId())).invokeSync();
            if (!goodsResponse.isSuccessful()) {
                Navigation.getNavigation(requireActivity()).removeOnBackListener(backListener);
                return;
            } else {
                incomingGood = Optional.ofNullable(goodsResponse.body())
                        .map(GoodsResponse::getItems)
                        .filter(Objects::nonNull)
                        .filter(f -> !f.isEmpty())
                        .map(m -> m.iterator().next())
                        .orElse(null);
            }
        } else {
            good = new Good();
        }
        Navigation.getNavigation(requireActivity()).addOnBackListener(backListener);
        if (good == null || good.getPrices().isEmpty()) {
            binding.tx1.setVisibility(View.GONE);
        }
        incomingGood = Objects.requireNonNull(good).clone();
        binding.goodCardName.setOnKeyListener(keyListener);
        binding.goodCardName.setText(good.getName());
        binding.goodCardPrice.setOnKeyListener(keyListener);
        binding.goodCardPrice.setText(MoneyUtils.getInstance().moneyWithCurrencyToString(good.getPrice()));
        binding.goodCardPrice.setOnClickListener(v -> MoneyPicker.builder(view.getContext())
                .setTitle("Укажите сумму")
                .setInitialValue(good.getPrice())
                .setMinValue(1)
                .setMaxValue(100000)
                .positiveButton(value -> {
                    binding.goodCardPrice.setText(String.format("%s", MoneyUtils.getInstance()
                            .moneyWithCurrencyToString(value)));
                    good.setPrice(value);
                })
                .build()
                .show());
        binding.goodCardDescription.setText(good.getDescription());
        binding.goodCardSave.setOnClickListener(v -> {
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

    private Boolean checkGoodOnOrders() {
        NetworkExecutor<Boolean> callback = new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getGoodsApi().checkGoodOnOrdersAvailability(good.getId()));
        Response<Boolean> booleanResponse = callback.invokeSync();
        if (!booleanResponse.isSuccessful()) {
            return null;
        }
        return booleanResponse.body();
    }

    private boolean save() {
        boolean fail = false;
        if (StringUtils.isEmpty(binding.goodCardName.getText().toString())) {
            binding.goodCardName.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (StringUtils.isEmpty(binding.goodCardPrice.getText().toString())) {
            binding.goodCardPrice.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_error));
            fail = true;
        }
        if (fail) {
            return false;
        }
        fillGood();
        Response<Void> voidResponse = new NetworkExecutor<>(requireActivity(),
                NetworkService.getInstance().getGoodsApi().saveGood(good)).invokeSync();
        incomingGood = good;
        return voidResponse.isSuccessful();
    }

    private void fillGood() {
        good = new Good();
        good.setId(incomingGood == null ? null : incomingGood.getId());
        good.setName(binding.goodCardName.getText().toString());
        good.setDescription(binding.goodCardDescription.getText().toString());
        good.setPrice(MoneyUtils.getInstance().stringToDouble(binding.goodCardPrice.getText().toString()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Navigation.getNavigation(requireActivity()).removeOnBackListener(backListener);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}

