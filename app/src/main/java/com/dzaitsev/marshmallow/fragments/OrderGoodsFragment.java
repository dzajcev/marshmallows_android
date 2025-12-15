package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.OrderLinesRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentOrderGoodsBinding;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.dto.bundles.OrderCardBundle;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class OrderGoodsFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "orderGoodsFragment";
    private FragmentOrderGoodsBinding binding;

    private OrderCardBundle orderCardBundle;

    private OrderLinesRecyclerViewAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        orderCardBundle = GsonHelper.deserialize(requireArguments().getString("orderCardBundle"), OrderCardBundle.class);
        binding = FragmentOrderGoodsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Позиции заказа");
        RecyclerView orderLinesList = view.findViewById(R.id.orderGoodsLinesList);
        binding.ordersGoodsForward.setOnClickListener(view1 -> {
            if (orderCardBundle.getOrderLines().isEmpty() || orderCardBundle.getOrderLines().stream().noneMatch(f -> f.getGood() != null)) {
                new StringUtils.ErrorDialog(requireActivity(), "Не заполнен перечень товаров").show();
            } else {
                Bundle bundle = new Bundle();
                int size = orderCardBundle.getOrderLines().size();
                orderCardBundle.getOrderLines().removeIf(f -> f.getGood() == null);
                try {
                    List<OrderLine> collect = orderCardBundle.getOrderLines().stream().collect(Collectors.groupingBy(x -> x.getGood().getId()))
                            .values()
                            .stream()
                            .map(m -> m.stream().reduce(null, (orderLine, orderLine2) -> {
                                if (orderLine == null) {
                                    orderLine = orderLine2.clone();
                                    orderLine.setCount(0);
                                }
                                if (!orderLine.getPrice().equals(orderLine2.getPrice())) {
                                    Toast.makeText(getContext(), "На один и тот же товар заданы разные цены", Toast.LENGTH_SHORT).show();
                                    throw new RuntimeException("different prices");
                                }
                                orderLine.setCount(orderLine.getCount()
                                        + (orderLine2.getCount() == null ? 0 : orderLine2.getCount()));
                                return orderLine;
                            })).collect(Collectors.toList());
                    orderCardBundle.setOrderLines(collect);
                    if (orderCardBundle.getOrderLines().size() != size) {
                        for (int i = 0; i < orderCardBundle.getOrderLines().size(); i++) {
                            orderCardBundle.getOrderLines().get(i).setNum(i + 1);
                        }
                    }
                    bundle.putString("orderCardBundle", GsonHelper.serialize(orderCardBundle));
                    Navigation.getNavigation().forward(OrderInfoFragment.IDENTITY, bundle);
                } catch (Exception e) {
//do nothing
                }

            }
        });
        binding.ordersGoodsBackward.setOnClickListener(view1 -> {
            if (!orderCardBundle.getOrderLines().isEmpty() && orderCardBundle.getOrderLines().stream().anyMatch(f -> f.getGood() != null)) {
                orderCardBundle.getOrderLines().removeIf(f -> f.getGood() == null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Есть не сохраненные данные. Продолжить?");
                builder.setPositiveButton("Да", (dialog, id) -> Navigation.getNavigation().back());
                builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
                builder.create().show();
            } else {
                Navigation.getNavigation().back();
            }
        });
        binding.orderGoodsLineAdd.setOnClickListener(v -> {
            OrderLine orderLine = new OrderLine();
            orderLine.setNum(mAdapter.getOriginalItems().stream().max(Comparator.comparing(OrderLine::getNum)).map(OrderLine::getNum).map(m -> m + 1).orElse(1));
            mAdapter.addItem(orderLine);
            Bundle bundle = new Bundle();
            bundle.putString("orderCardBundle", GsonHelper.serialize(orderCardBundle));
            bundle.putInt("orderline", orderLine.getNum());
            bundle.putString("source", "orderCard");
            Navigation.getNavigation().forward(GoodsFragment.IDENTITY, bundle);
        });
        orderLinesList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new OrderLinesRecyclerViewAdapter();
        mAdapter.setRemoveListener(position -> {
            if (position >= 0) {
                for (int i = position; i < mAdapter.getOriginalItems().size(); i++) {
                    mAdapter.getOriginalItems().get(i).setNum(mAdapter.getOriginalItems().get(i).getNum() - 1);
                }
            }
            orderLinesList.setAdapter(mAdapter);
            mAdapter.setItems(mAdapter.getOriginalItems());
        });
        mAdapter.setSelectGoodListener(orderLine -> {
            Bundle bundle = new Bundle();
            bundle.putString("orderCardBundle", GsonHelper.serialize(orderCardBundle));
            bundle.putInt("orderline", orderLine.getNum());
            bundle.putString("source", "orderGoods");
            Navigation.getNavigation().forward(GoodsFragment.IDENTITY, bundle);
        });
        mAdapter.setChangeSumListener(() -> binding.orderGoodsSum.setText(MoneyUtils.moneyWithCurrencyToString(calsSum(mAdapter.getOriginalItems()))));
        mAdapter.setItems(orderCardBundle.getOrderLines());
        orderLinesList.setAdapter(mAdapter);
        binding.orderGoodsSum.setText(MoneyUtils.moneyWithCurrencyToString(calsSum(mAdapter.getOriginalItems())));
        if (orderCardBundle.getOrderLines().isEmpty()) {
            binding.orderGoodsLinesList.callOnClick();
        }
    }

    private Double calsSum(List<OrderLine> orderLines) {
        return orderLines.stream().mapToDouble(m -> Optional.ofNullable(m.getPrice()).orElse(0d)
                * Optional.ofNullable(m.getCount()).orElse(0)).sum();
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