package com.dzaitsev.marshmallow.utils.orderfilter;

import android.content.SharedPreferences;

public class FiltersHelperInitializer {

    public static void init(SharedPreferences preferences) {
        FiltersHelper instance = FiltersHelper.getInstance();
        instance.setPreferences(preferences);
    }
}
