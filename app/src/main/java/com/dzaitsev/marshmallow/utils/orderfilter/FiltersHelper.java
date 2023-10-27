package com.dzaitsev.marshmallow.utils.orderfilter;

import android.content.SharedPreferences;

import com.dzaitsev.marshmallow.dto.DeliveryFilter;
import com.dzaitsev.marshmallow.dto.OrdersFilter;
import com.dzaitsev.marshmallow.utils.GsonExt;

import java.util.Optional;

public class FiltersHelper {
    private final static String orderFilterData = "order-filter";
    private final static String deliveryFilterData = "delivery-filter";

    private static FiltersHelper filtersHelper;
    private SharedPreferences preferences;

    public static FiltersHelper getInstance() {
        if (filtersHelper == null) {
            filtersHelper = new FiltersHelper();
        }
        return filtersHelper;
    }

    protected void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public Optional<OrdersFilter> getOrderFilter() {
        return Optional.ofNullable(GsonExt.getGson()
                .fromJson(preferences.getString(orderFilterData, ""), OrdersFilter.class));
    }
    public void updateOrderFilter(OrdersFilter filter) {
        preferences.edit()
                .putString(orderFilterData, GsonExt.getGson().toJson(filter))
                .apply();
    }
    public Optional<DeliveryFilter> getDeliveryFilter() {
        return Optional.ofNullable(GsonExt.getGson()
                .fromJson(preferences.getString(deliveryFilterData, ""), DeliveryFilter.class));
    }

    public void updateDeliveryFilter(DeliveryFilter filter) {
        preferences.edit()
                .putString(deliveryFilterData, GsonExt.getGson().toJson(filter))
                .apply();
    }

}
