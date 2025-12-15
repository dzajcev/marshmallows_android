package com.dzaitsev.marshmallow.utils.navigation;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationInitializer {

    public static void init(FragmentActivity activity, BottomNavigationView bottomNavigationView) {
        Navigation.getNavigation()
                .attachActivity(activity)
                .attachBottomNav(bottomNavigationView);
    }
}
