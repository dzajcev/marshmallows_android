package com.dzaitsev.marshmallow.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dzaitsev.marshmallow.ErrorDialog;
import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.adapters.GoodsListAdapter;
import com.dzaitsev.marshmallow.databinding.FragmentGoodsBinding;
import com.dzaitsev.marshmallow.databinding.FragmentOrdersBinding;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;
import com.dzaitsev.marshmallow.service.NetworkService;

import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private GoodsListAdapter mAdapter;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        final RecyclerView ordersList = view.findViewById(R.id.goodsList);

        binding.goods.setOnClickListener(view1 -> {
            NavHostFragment.findNavController(OrdersFragment.this)
                    .navigate(R.id.action_ordersFragment_to_goodsFragment);
        });
        binding.newOrder.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("order", new Order());
            NavHostFragment.findNavController(OrdersFragment.this)
                    .navigate(R.id.action_ordersFragment_to_orderGoodsFragment, bundle);
        });
//        try {
//            NetworkService.getInstance().getMarshmallowApi().getGoods().enqueue(new Callback<GoodsResponse>() {
//                @Override
//                public void onResponse(Call<GoodsResponse> call, Response<GoodsResponse> response) {
//                    requireActivity().runOnUiThread(() -> mAdapter.setItems(Optional.ofNullable(response.body())
//                            .orElse(new GoodsResponse()).getGoods()));
//                }
//
//                @Override
//                public void onFailure(Call<GoodsResponse> call, Throwable t) {
//                    new ErrorDialog(requireActivity(), t.getMessage()).show();
//                }
//            });
//
//        } catch (Exception e) {
//            new ErrorDialog(requireActivity(), e.getMessage()).show();
//        }
//        goodsList.setLayoutManager(new LinearLayoutManager(view.getContext()));
//        mAdapter = new GoodsListAdapter();
//        mAdapter.setSelectGoodItemListener(good -> {
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("good", good);
//            NavHostFragment.findNavController(OrdersFragment.this)
//                    .navigate(R.id.action_goodsFragment_to_goodCard, bundle);
//        });
//        goodsList.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}