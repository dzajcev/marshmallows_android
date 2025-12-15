package com.dzaitsev.marshmallow.utils.navigation;

import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.fragments.ClientCardFragment;
import com.dzaitsev.marshmallow.fragments.ClientsFragment;
import com.dzaitsev.marshmallow.fragments.DeliveriesFragment;
import com.dzaitsev.marshmallow.fragments.GoodCardFragment;
import com.dzaitsev.marshmallow.fragments.GoodsFragment;
import com.dzaitsev.marshmallow.fragments.LoginFragment;
import com.dzaitsev.marshmallow.fragments.OrderFilterFragment;
import com.dzaitsev.marshmallow.fragments.OrderFragment;
import com.dzaitsev.marshmallow.fragments.OrderInfoFragment;
import com.dzaitsev.marshmallow.fragments.OrdersFragment;

public final class FragmentFactory {

    public static Fragment create(String id) {
        return switch (id) {
            case OrdersFragment.IDENTITY -> new OrdersFragment();
            case GoodsFragment.IDENTITY -> new GoodsFragment();
            case ClientsFragment.IDENTITY -> new ClientsFragment();
            case DeliveriesFragment.IDENTITY -> new DeliveriesFragment();
            case LoginFragment.IDENTITY -> new LoginFragment();
            case OrderFragment.IDENTITY -> new OrderFragment();
            case OrderFilterFragment.IDENTITY -> new OrderFilterFragment();
            case OrderInfoFragment.IDENTITY -> new OrderInfoFragment();
            case ClientCardFragment.IDENTITY -> new ClientCardFragment();
            case GoodCardFragment.IDENTITY -> new GoodCardFragment();
            default -> throw new IllegalStateException("Unknown fragment: " + id);
        };
    }
}