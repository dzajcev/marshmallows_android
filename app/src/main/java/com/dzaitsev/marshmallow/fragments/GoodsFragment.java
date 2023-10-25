package com.dzaitsev.marshmallow.fragments;

import android.os.Bundle;

import com.dzaitsev.marshmallow.Navigation;
import com.dzaitsev.marshmallow.adapters.GoodRecyclerViewAdapter;
import com.dzaitsev.marshmallow.adapters.listeners.SelectItemListener;
import com.dzaitsev.marshmallow.dto.Good;
import com.dzaitsev.marshmallow.dto.Order;
import com.dzaitsev.marshmallow.dto.response.GoodsResponse;
import com.dzaitsev.marshmallow.service.NetworkService;

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
                .map(m -> m.getSerializable("order", Order.class)).orElse(null);
        setOnCreateListener(() -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("good", new Good());
            Navigation.getNavigation(requireActivity()).goForward(new GoodCardFragment(), bundle);
        });
        setEditItemListener(good -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("good", good);
            Navigation.getNavigation(requireActivity()).goForward(new GoodCardFragment(), bundle);
        });
        if (order != null) {
            Integer orderline = Optional.ofNullable(getArguments())
                    .map(m -> m.getSerializable("orderline", Integer.class)).orElse(null);
            setSelectListener(Optional.of(order)
                    .map(m -> (SelectItemListener<Good>) item -> {
                        Bundle bundle = new Bundle();
                        order.getOrderLines().stream()
                                .filter(f -> f.getNum().equals(orderline))
                                .findAny().ifPresent(orderLine -> {
                                    if (orderLine.getGood() == null) {
                                        order.getOrderLines().stream()
                                                .filter(f -> f.getGood() != null)
                                                .filter(f -> f.getGood().getId().equals(item.getId()))
                                                .findAny()
                                                .ifPresentOrElse(orderLine12 -> {
                                                    orderLine12.setDone(false);
                                                    orderLine12.setCount(orderLine12.getCount() + 1);
                                                    order.getOrderLines().remove(orderLine);
                                                }, () -> {
                                                    orderLine.setGood(item);
                                                    orderLine.setPrice(item.getPrice());
                                                    orderLine.setCount(1);
                                                });
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
                        bundle.putSerializable("order", order);
                        Navigation.getNavigation(requireActivity()).back(bundle);
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