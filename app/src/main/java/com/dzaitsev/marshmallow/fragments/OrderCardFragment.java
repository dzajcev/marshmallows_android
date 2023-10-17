package com.dzaitsev.marshmallow.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.MainActivity;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.OrderLinesRecyclerViewAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentOrderCardBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.service.NetworkExecutor;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class OrderCardFragment extends Fragment {

    private FragmentOrderCardBinding binding;


    private Order incomingOrder;
    private EditText goodName;
    private EditText goodPrice;

    private Order order;

    private OrderLinesRecyclerViewAdapter mAdapter;

    private final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (hasChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Запись изменена. Сохранить?");
                builder.setPositiveButton("Да", (dialog, id) -> {
                    if (save()) {
                        setEnabled(false);
                        requireActivity().onBackPressed();
                    }
                });
                builder.setNeutralButton("Отмена", (dialog, id) -> dialog.cancel());
                builder.setNegativeButton("Нет", (dialog, id) -> {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                });
                builder.create().show();
            } else {
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        }
    };

    private boolean hasChanges() {
        Order order = constructOrder();
        return !order.equals(incomingOrder);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        order = requireArguments().getSerializable("order", Order.class);
        binding = FragmentOrderCardBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private View.OnKeyListener keyListener = (v, keyCode, event) -> {
        v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.field_background));
        return false;
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView orderLinesList = view.findViewById(R.id.orderLinesList);
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
//        mAdapter.setChangeSumListener(() -> binding.orderGoodsSum.setText(MoneyUtils.getInstance()
//                .moneyWithCurrencyToString(calsSum(mAdapter.getItems()))));
        mAdapter.setDoneListener(new OrderLinesRecyclerViewAdapter.DoneListener() {
            @Override
            public void onDone(OrderLine orderLine, View v) {
                Drawable background = v.getBackground();
                System.out.println();
            }
        });
        orderLinesList.setAdapter(mAdapter);
        order.getOrderLines().sort(Comparator.comparing(OrderLine::getNum));
        mAdapter.setItems(order.getOrderLines());
    }

    private boolean save() {
        return true;
//        boolean fail = false;
//        if (goodName.getText() == null || goodName.getText().toString().isEmpty()) {
//            goodName.setBackgroundColor(Color.parseColor("#fa8c8c"));
//            fail = true;
//        }
//        if (goodPrice.getText() == null || goodPrice.getText().toString().isEmpty()) {
//            goodPrice.setBackgroundColor(Color.parseColor("#fa8c8c"));
//            fail = true;
//        }
//        if (fail) {
//            return false;
//        }
//        Order order = constructOrder();
////        CountDownLatch countDownLatch = new CountDownLatch(1);
////        NetworkExecutor<Void> callback = new NetworkExecutor<>(requireActivity(),
////                response -> countDownLatch.countDown(), countDownLatch);
////        NetworkService.getInstance().getMarshmallowApi().saveOrder(order)
////                .enqueue(callback);
//        incomingOrder = order;
//        return true;
    }

    private Order constructOrder() {
        Order good = new Order();
////        good.setId(incomingOrder == null ? null : incomingOrder.getId());
////        good.setName(goodName.getText().toString());
////        try {
////            if (goodPrice.getText() != null && !goodPrice.getText().toString().isEmpty()) {
////                good.setPrice(Optional.ofNullable(formatter.parse(goodPrice.getText().toString())).orElse(-1d).doubleValue());
////            }
////        } catch (ParseException e) {
////            throw new RuntimeException(e);
////        }
//        return good;
        return good;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        FragmentActivity fragmentActivity = requireActivity();
        if (fragmentActivity instanceof MainActivity) {
            MainActivity ma = (MainActivity) fragmentActivity;
            ma.setNavigationBackListener(null);
        }
    }

}