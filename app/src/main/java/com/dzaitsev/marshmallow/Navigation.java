package com.dzaitsev.marshmallow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Navigation {

    private final FragmentManager fragmentManager;


    private final LinkedList<Fragment> backStack = new LinkedList<>();
    private final Map<Integer, Bundle> incomingBundles = new HashMap<>();


    private static Navigation navigation;

    public Navigation(FragmentActivity activity) {
        this.fragmentManager = activity.getSupportFragmentManager();
    }


    public static Navigation getNavigation(FragmentActivity activity) {
        if (navigation == null) {
            navigation = new Navigation(activity);
        }
        return navigation;
    }

    public void goForward(Fragment fragment) {
        goForward(fragment, null);
    }

    public void goForward(Fragment fragment, Bundle bundle) {
        backStack.add(fragment);
        incomingBundles.put(fragment.getId(), bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    public void back() {
        back(null);
    }

    public void back(Bundle bundle) {
        backStack.removeLast();

        Fragment last = backStack.getLast();
        if (bundle == null) {
            bundle = incomingBundles.get(last.getId());
        }
        goForward(last, bundle);
        backStack.remove(last);
    }
}
