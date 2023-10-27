package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;

import com.dzaitsev.marshmallow.adapters.GoodRecyclerViewAdapter;
import com.dzaitsev.marshmallow.adapters.listeners.SelectItemListener;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.OrderLine;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonHelper;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;

import java.util.Optional;

import retrofit2.Call;

public class GoodsFragment extends AbstractNsiFragment<Good, GoodsResponse, GoodRecyclerViewAdapter> {

    public static final String IDENTITY = "goodsFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAdapter(new GoodRecyclerViewAdapter());
        requireActivity().setTitle("Зефирки и прочее");
        Order order = Optional.ofNullable(getArguments())
                .map(m -> GsonHelper.deserialize(m.getString("order"), Order.class)).orElse(null);
        setOnCreateListener(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("good", GsonHelper.serialize(new Good()));
            Navigation.getNavigation().goForward(new GoodCardFragment(), bundle);
        });
        setEditItemListener(good -> {
            Bundle bundle = new Bundle();
            bundle.putString("good",GsonHelper.serialize( good));
            Navigation.getNavigation().goForward(new GoodCardFragment(), bundle);
        });
        if (order != null) {
            Integer orderline = getArguments().getInt("orderline");
            setSelectListener(Optional.of(order)
                    .map(m -> (SelectItemListener<Good>) item -> {
                        Bundle bundle = new Bundle();
                        order.getOrderLines().stream()
                                .filter(f -> f.getNum().equals(orderline))
                                .findAny().ifPresent(orderLine -> {
                                    if (orderLine.getGood() == null) {
                                        Optional<OrderLine> any = order.getOrderLines().stream()
                                                .filter(f -> f.getGood() != null)
                                                .filter(f -> f.getGood().getId().equals(item.getId()))
                                                .findAny();
                                        if (any.isPresent()) {
                                            OrderLine orderLine12 = any.get();
                                            orderLine12.setDone(false);
                                            orderLine12.setCount(orderLine12.getCount() + 1);
                                            order.getOrderLines().remove(orderLine);
                                        } else {
                                            orderLine.setGood(item);
                                            orderLine.setPrice(item.getPrice());
                                            orderLine.setCount(1);
                                        }
                                    } else {
                                        orderLine.setDone(false);
                                        if (orderLine.getGood().getId().equals(item.getId())) {
                                            orderLine.setCount(orderLine.getCount() + 1);
                                        } else {
                                            order.getOrderLines().stream()
                                                    .filter(f -> f.getGood().getId().equals(item.getId()))
                                                    .findAny()
                                                    .ifPresent(orderLine1 -> {
                                                        orderLine1.setCount(orderLine1.getCount() + 1);
                                                        order.getOrderLines().remove(orderLine);
                                                    });

                                            orderLine.setGood(item);
                                            orderLine.setPrice(item.getPrice());
                                        }
                                    }
                                    order.getOrderLines().removeIf(f -> f.getGood() == null);
                                });
                        bundle.putString("order", GsonHelper.serialize(order));
                        Navigation.getNavigation().back(bundle);
                    })
                    .orElse(null));
        }
    }

    @Override
    protected Call<GoodsResponse> getCall(Boolean bool) {
        return NetworkService.getInstance().getGoodsApi().getGoods(bool);
    }

    @Override
    public String getUniqueName() {
        return IDENTITY;
    }
}