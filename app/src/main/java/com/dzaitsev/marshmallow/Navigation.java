package com.dzaitsev.marshmallow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.dzaitsev.marshmallow.fragments.ClientsFragment;
import com.dzaitsev.marshmallow.fragments.DeliveriesFragment;
import com.dzaitsev.marshmallow.fragments.GoodsFragment;
import com.dzaitsev.marshmallow.fragments.IdentityFragment;
import com.dzaitsev.marshmallow.fragments.LoginFragment;
import com.dzaitsev.marshmallow.fragments.OrdersFragment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Navigation {

    //    private final FragmentManager fragmentManager;
    private FragmentActivity fragmentActivity;

    private final LinkedList<Fragment> backStack = new LinkedList<>();
    private final Map<String, Bundle> incomingBundles = new HashMap<>();

    private final LinkedList<OnBackListener> onBackListeners = new LinkedList<>();

    private static Navigation navigation;

    private FragmentManager fragmentManager() {
        return fragmentActivity.getSupportFragmentManager();
    }

    public Navigation(FragmentActivity activity) {
        this.fragmentActivity = activity;
        fragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof IdentityFragment identityFragment) {
                if ((getRootFragments().stream()
                        .anyMatch(a -> a.equals(identityFragment.getUniqueName())))
                        && (fragment.getArguments() == null || fragment.getArguments().isEmpty())
                        || ((IdentityFragment) fragment).getUniqueName().equals(LoginFragment.IDENTITY)) {
                    backStack.clear();
                    backStack.add(fragment);
                }
            }
        });
    }


    public static Navigation getNavigation(FragmentActivity activity) {
        if (navigation == null) {
            navigation = new Navigation(activity);
        }
        navigation.fragmentActivity = activity;
        return navigation;
    }

    public List<String> getRootFragments() {
        return Stream.of(OrdersFragment.IDENTITY, GoodsFragment.IDENTITY, ClientsFragment.IDENTITY, DeliveriesFragment.IDENTITY)
                .collect(Collectors.toList());
    }

    public void goForward(Fragment fragment) {
        goForward(fragment, null);
    }

    public void goForward(Fragment fragment, Bundle bundle) {
        backStack.add(fragment);
        if (fragment instanceof IdentityFragment identityFragment) {
            incomingBundles.put(identityFragment.getUniqueName(), bundle);
        }
        fragment.setArguments(bundle);
        if (!fragmentManager().isDestroyed()) {
            fragmentManager().beginTransaction()
                    .replace(R.id.frame, fragment)
                    .commit();
        }
    }

    public void back() {
        back(null);
    }

    public void back(Bundle bundle) {
        Fragment l = backStack.getLast();
        if (l instanceof IdentityFragment identityFragment) {
            if (getRootFragments().stream()
                    .anyMatch(a -> a.equals(identityFragment.getUniqueName()))
                    && (l.getArguments() == null || l.getArguments().isEmpty())) {
                fragmentActivity.finish();
                System.exit(0);
            }
        }
        backStack.removeLast();
        Fragment last = backStack.getLast();

        if (bundle == null) {
            if (last instanceof IdentityFragment identityFragment) {
                bundle = incomingBundles.get(identityFragment.getUniqueName());
            }
        }
        goForward(last, bundle);
        backStack.remove(last);
    }

    public void callbackBack() {
        Fragment last = backStack.getLast();
        if (onBackListeners.isEmpty() ||

                onBackListeners.getLast().onBack(last)) {
            back();
        }
    }

    public void addOnBackListener(OnBackListener onBackListener) {
        this.onBackListeners.add(onBackListener);
    }

    public void removeOnBackListener(OnBackListener onBackListener) {
        this.onBackListeners.remove(onBackListener);
    }

    public interface OnBackListener {
        boolean onBack(Fragment fragment);
    }
}
