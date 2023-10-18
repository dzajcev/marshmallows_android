package com.dzaitsev.marshmallow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dzaitsev.marshmallow.fragments.Identity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Navigation {

    private final FragmentManager fragmentManager;
    private final FragmentActivity fragmentActivity;

    private final LinkedList<Fragment> backStack = new LinkedList<>();
    private final Map<String, Bundle> incomingBundles = new HashMap<>();

    private OnBackListener onBackListener;

    private static Navigation navigation;


    public Navigation(FragmentActivity activity) {
        this.fragmentManager = activity.getSupportFragmentManager();
        this.fragmentActivity = activity;
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
        if (fragment instanceof Identity identity) {
            incomingBundles.put(identity.getUniqueName(), bundle);
        }

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
        if (backStack.isEmpty()) {
            fragmentActivity.finish();
            System.exit(0);
        }
        Fragment last = backStack.getLast();
        if (bundle == null) {
            if (last instanceof Identity identity) {
                bundle = incomingBundles.get(identity.getUniqueName());
            }
        }
        goForward(last, bundle);
        backStack.remove(last);

    }

    public void callbackBack() {
        if (onBackListener == null || onBackListener.onBack(backStack.getLast())) {
            back();
        }
    }

    public void setOnBackListener(OnBackListener onBackListener) {
        this.onBackListener = onBackListener;
    }

    public interface OnBackListener {
        boolean onBack(Fragment fragment);
    }
}
