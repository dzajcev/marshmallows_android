package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.OrderLinesRecyclerViewAdapter;
import com.dzaitsev.marshmallow.components.OrderSharedViewModel;
import com.dzaitsev.marshmallow.databinding.FragmentOrderLinesBinding;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.dto.bundles.OrderCardBundle;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;

import java.util.Comparator;

public class OrderLinesFragment extends Fragment implements IdentityFragment {
    public static final String IDENTITY = "orderCardLinesFragment";
    private OrderLinesRecyclerViewAdapter mAdapter;
    private OrderSharedViewModel viewModel;

    public OrderLinesFragment() {

    }

    private FragmentOrderLinesBinding binding;

    private OrderCardBundle getOrderInfoBundle() {
        Fragment parent = getParentFragment();
        if (parent instanceof OrderFragment orderFragment) {
            return orderFragment.getOrderCardBundle();
        }
        throw new IllegalStateException("Parent must be OrderFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment())
                .get(OrderSharedViewModel.class);
        updateUI();
    }

    private void updateUI() {
        mAdapter = new OrderLinesRecyclerViewAdapter();
        assert getView() != null;
        RecyclerView orderLinesList = getView().findViewById(R.id.orderLines);
        orderLinesList.setLayoutManager(new LinearLayoutManager(getView().getContext()));
        orderLinesList.setAdapter(mAdapter);

        getOrderInfoBundle().getOrderLines().sort(Comparator.comparing(OrderLine::getNum));
        mAdapter.setItems(getOrderInfoBundle().getOrderLines());
        mAdapter.setChangeSumListener(() -> viewModel.notifySumsChanged());
        viewModel.getOrderLiveData().observe(getViewLifecycleOwner(), order -> {
            if (order == null) return;
            binding.btnAddPosition.post(() -> {
                if (order.getOrderStatus().isEditable()) {
                    binding.btnAddPosition.show();
                } else {
                    binding.btnAddPosition.hide();
                }
            });
        });

        if (getOrderInfoBundle().getOrder().getOrderStatus().isEditable()) {
            mAdapter.setDoneListener((orderLine, v) -> {
                orderLine.setDone(!orderLine.isDone());
                viewModel.notifyDoneChanged();
            });
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

                bundle.putString("orderCardBundle", GsonHelper.serialize(getOrderInfoBundle()));
                bundle.putInt("orderline", orderLine.getNum());
                bundle.putString("source", "orderCard");
                Navigation.getNavigation().forward(GoodsFragment.IDENTITY, bundle);
            });
            binding.btnAddPosition.setOnClickListener(v -> {
                OrderLine orderLine = new OrderLine();
                orderLine.setNum(mAdapter.getOriginalItems().stream()
                        .max(Comparator.comparing(OrderLine::getNum))
                        .map(OrderLine::getNum).map(m -> m + 1).orElse(1));
                mAdapter.addItem(orderLine);
                Bundle bundle = new Bundle();
                getOrderInfoBundle().getOrderLines().add(orderLine);
                bundle.putString("orderCardBundle", GsonHelper.serialize(getOrderInfoBundle()));
                bundle.putInt("orderline", orderLine.getNum());
                bundle.putString("source", "orderCard");

                Navigation.getNavigation().forward(GoodsFragment.IDENTITY, bundle);
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrderLinesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}