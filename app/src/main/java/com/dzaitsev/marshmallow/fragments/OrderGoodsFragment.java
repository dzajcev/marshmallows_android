package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.OrderLinesRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentOrderGoodsBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class OrderGoodsFragment extends Fragment {

    private FragmentOrderGoodsBinding binding;

    private Order order;

    private OrderLinesRecyclerViewAdapter mAdapter;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        order = requireArguments().getSerializable("order", Order.class);
        binding = FragmentOrderGoodsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView orderLinesList = view.findViewById(R.id.orderGoodsLinesList);
        binding.ordersGoodsForward.setOnClickListener(view1 -> {
            if (order.getOrderLines().isEmpty() || order.getOrderLines().stream().noneMatch(f -> f.getGood() != null)) {
                new StringUtils.ErrorDialog(requireActivity(), "Не заполнен перечень товаров").show();
            } else {
                Bundle bundle = new Bundle();
                int size = order.getOrderLines().size();
                order.getOrderLines().removeIf(f -> f.getGood() == null);
                try {
                    List<OrderLine> collect = order.getOrderLines().stream().collect(Collectors.groupingBy(x -> x.getGood().getId()))
                            .values()
                            .stream()
                            .map(m -> m.stream().reduce(null, (orderLine, orderLine2) -> {
                                if (orderLine == null) {
                                    orderLine = orderLine2.clone();
                                    orderLine.setCount(0);
                                }
                                if (!orderLine.getPrice().equals(orderLine2.getPrice())) {
                                    Toast.makeText(getContext(),"На один и тот же товар заданы разные цены", Toast.LENGTH_SHORT).show();
                                    throw new RuntimeException("different prices");
                                }
                                orderLine.setCount(orderLine.getCount()
                                        + (orderLine2.getCount() == null ? 0 : orderLine2.getCount()));
                                return orderLine;
                            })).collect(Collectors.toList());
                    order.setOrderLines(collect);
                    if (order.getOrderLines().size() != size) {
                        for (int i = 0; i < order.getOrderLines().size(); i++) {
                            order.getOrderLines().get(i).setNum(i + 1);
                        }
                    }
                    bundle.putSerializable("order", order);
                    NavHostFragment.findNavController(OrderGoodsFragment.this).navigate(R.id.action_orderGoodsFragment_to_orderClientFragment, bundle);
                } catch (Exception e) {
//do nothing
                }

            }
        });
        binding.ordersGoodsBackward.setOnClickListener(view1 -> {
            if (!order.getOrderLines().isEmpty() && order.getOrderLines().stream().anyMatch(f -> f.getGood() != null)) {
                order.getOrderLines().removeIf(f -> f.getGood() == null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Есть не сохраненные данные. Продолжить?");
                builder.setPositiveButton("Да", (dialog, id) -> NavHostFragment.findNavController(OrderGoodsFragment.this).navigate(R.id.action_orderGoodsFragment_to_ordersFragment));
                builder.setNegativeButton("Нет", (dialog, id) -> dialog.cancel());
                builder.create().show();
            } else {
                NavHostFragment.findNavController(OrderGoodsFragment.this).navigate(R.id.action_orderGoodsFragment_to_ordersFragment);
            }
        });
        binding.orderGoodsLineAdd.setOnClickListener(v -> {
            OrderLine orderLine = new OrderLine();
            orderLine.setNum(mAdapter.getItems().stream().max(Comparator.comparing(OrderLine::getNum)).map(OrderLine::getNum).map(m -> m + 1).orElse(1));
            mAdapter.addLine(orderLine);
        });
        orderLinesList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new OrderLinesRecyclerViewAdapter();
        mAdapter.setRemoveListener(position -> {
            List<OrderLine> items = mAdapter.getItems();
            items.remove(position);
            for (int i = position; i < items.size(); i++) {
                items.get(i).setNum(items.get(i).getNum() - 1);
            }
            orderLinesList.setAdapter(mAdapter);
            mAdapter.setItems(items);
        });
        mAdapter.setSelectGoodListener(orderLine -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("order", order);
            bundle.putInt("orderline", orderLine.getNum());
            NavHostFragment.findNavController(OrderGoodsFragment.this).navigate(R.id.action_orderGoodsFragment_to_goodsFragment, bundle);
        });
        mAdapter.setChangeSumListener(() -> binding.orderGoodsSum.setText(MoneyUtils.getInstance()
                .moneyWithCurrencyToString(calsSum(mAdapter.getItems()))));
        mAdapter.setItems(order.getOrderLines());
        orderLinesList.setAdapter(mAdapter);
        binding.orderGoodsSum.setText(MoneyUtils.getInstance()
                .moneyWithCurrencyToString(calsSum(mAdapter.getItems())));
        if (order.getOrderLines().isEmpty()) {
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
}