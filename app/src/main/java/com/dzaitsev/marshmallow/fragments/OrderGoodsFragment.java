package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.OrderLinesListAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentOrderGoodsBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.utils.MoneyUtils;
import com.dzaitsev.marshmallow.utils.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public class OrderGoodsFragment extends Fragment {

    private FragmentOrderGoodsBinding binding;

    private Order order;

    private OrderLinesListAdapter mAdapter;

    private RecyclerView orderLinesList;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        order = requireArguments().getSerializable("order", Order.class);
        binding = FragmentOrderGoodsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderLinesList = view.findViewById(R.id.orderLinesList);
        binding.ordersGoodsForward.setOnClickListener(view1 -> {
            if (order.getOrderLines().isEmpty() || order.getOrderLines().stream().noneMatch(f -> f.getGood() != null)) {
                new StringUtils.ErrorDialog(requireActivity(), "Не заполнен перечень товаров").show();
            } else {
                Bundle bundle = new Bundle();
                order.getOrderLines().removeIf(f -> f.getGood() == null);
                bundle.putSerializable("order", order);
                NavHostFragment.findNavController(OrderGoodsFragment.this).navigate(R.id.action_orderGoodsFragment_to_orderClientFragment, bundle);
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
        binding.orderLineAdd.setOnClickListener(v -> {
            OrderLine orderLine = new OrderLine();
            orderLine.setNum(mAdapter.getItems().stream().max(Comparator.comparing(OrderLine::getNum)).map(OrderLine::getNum).map(m -> m + 1).orElse(1));
            mAdapter.addLine(orderLine);
        });
        orderLinesList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new OrderLinesListAdapter(getOrdersStatus(order));
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
            binding.orderLineAdd.callOnClick();
        }

    }

    private Double calsSum(List<OrderLine> orderLines) {
        return orderLines.stream().mapToDouble(m -> Optional.ofNullable(m.getPrice()).orElse(0d)
                * Optional.ofNullable(m.getCount()).orElse(0)).sum();
    }

    private OrderStatus getOrdersStatus(Order order) {
        if (order.getId() == null) {
            return OrderStatus.NEW;
        } else if (order.getShipped()) {
            return OrderStatus.DONE;
        } else {
            return OrderStatus.IN_PROGRESS;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}