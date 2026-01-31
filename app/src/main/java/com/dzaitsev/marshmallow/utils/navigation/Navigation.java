package com.dzaitsev.marshmallow.utils.navigation;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.UserRole;
import com.dzaitsev.marshmallow.fragments.ClientsFragment;
import com.dzaitsev.marshmallow.fragments.DeliveriesFragment;
import com.dzaitsev.marshmallow.fragments.GoodsFragment;
import com.dzaitsev.marshmallow.fragments.IdentityFragment;
import com.dzaitsev.marshmallow.fragments.LoginFragment;
import com.dzaitsev.marshmallow.fragments.OrdersFragment;
import com.dzaitsev.marshmallow.fragments.RegistrationFragment;
import com.dzaitsev.marshmallow.fragments.ConfirmRegistrationFragment;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Navigation {

    /* ======================= STATE ======================= */

    private FragmentActivity activity;
    private BottomNavigationView bottomNavigationView;

    private final LinkedList<NavEntry> backStack = new LinkedList<>();
    private static Navigation instance;
    private final LinkedList<OnBackListener> onBackListeners = new LinkedList<>();

    private Navigation() {
    }

    public static Navigation getNavigation() {
        if (instance == null) instance = new Navigation();
        return instance;
    }

    /* ======================= INIT ======================= */

    public Navigation attachActivity(FragmentActivity activity) {
        this.activity = activity;

        activity.getSupportFragmentManager()
                .addFragmentOnAttachListener((fm, fragment) -> {
                    if (fragment instanceof IdentityFragment id) {
                        if (isRoot(id.getUniqueName())
                                && (fragment.getArguments() == null || fragment.getArguments().isEmpty())
                                || id.getUniqueName().equals(LoginFragment.IDENTITY)) {

                            backStack.clear();
                            backStack.add(new NavEntry(id.getUniqueName(), fragment.getArguments()));
                        }
                    }
                });

        return this;
    }

    public Navigation attachBottomNav(BottomNavigationView nav) {
        this.bottomNavigationView = nav;

        nav.setOnItemSelectedListener(item -> {
            Fragment current = fragmentManager().findFragmentById(R.id.frame);

            if (item.getItemId() == R.id.ordersMenu && !(current instanceof OrdersFragment)) {
                forward(OrdersFragment.IDENTITY, null);
                return true;
            }
            if (item.getItemId() == R.id.goodsMenu && !(current instanceof GoodsFragment)) {
                forward(GoodsFragment.IDENTITY, null);
                return true;
            }
            if (item.getItemId() == R.id.clientsMenu && !(current instanceof ClientsFragment)) {
                forward(ClientsFragment.IDENTITY, null);
                return true;
            }
            if (item.getItemId() == R.id.deliveryMenu && !(current instanceof DeliveriesFragment)) {
                forward(DeliveriesFragment.IDENTITY, null);
                return true;
            }
            return false;
        });

        fragmentManager().addFragmentOnAttachListener((fm, fragment) -> {
            if (bottomNavigationView == null) return;

            AuthorizationHelper.getInstance().getUserData().ifPresentOrElse(user -> {
                if (user.getRole() == UserRole.DELIVERYMAN) {
                    bottomNavigationView.setVisibility(View.GONE);
                    return;
                }

                if (fragment instanceof IdentityFragment id
                        && isRoot(id.getUniqueName())
                        && (fragment.getArguments() == null || fragment.getArguments().isEmpty())) {

                    bottomNavigationView.setVisibility(View.VISIBLE);

                    switch (id.getUniqueName()) {
                        case OrdersFragment.IDENTITY ->
                                nav.getMenu().findItem(R.id.ordersMenu).setChecked(true);
                        case GoodsFragment.IDENTITY ->
                                nav.getMenu().findItem(R.id.goodsMenu).setChecked(true);
                        case ClientsFragment.IDENTITY ->
                                nav.getMenu().findItem(R.id.clientsMenu).setChecked(true);
                        case DeliveriesFragment.IDENTITY ->
                                nav.getMenu().findItem(R.id.deliveryMenu).setChecked(true);
                    }
                } else {
                    bottomNavigationView.setVisibility(View.GONE);
                }
            }, () -> {
                bottomNavigationView.setVisibility(View.GONE);
            });
        });

        return this;
    }

    /* ======================= NAVIGATION ======================= */

    public void forward(String identity) {
        forward(identity, null);
    }

    public void forward(String identity, Bundle args) {
        Fragment fragment = FragmentFactory.create(identity);
        fragment.setArguments(args);

        backStack.add(new NavEntry(identity, args));

        fragmentManager().beginTransaction()
                .replace(R.id.frame, fragment)
                .commitAllowingStateLoss();
    }

    public void back() {
        back(null);
    }

    public void back(Bundle result) {
        if (backStack.size() <= 1) {
            activity.finish();
            return;
        }

        backStack.removeLast();

        NavEntry entry = backStack.getLast();
        Bundle bundle;
        if (result == null) {
            bundle = entry.args();
        } else {
            bundle = result;
        }
        Fragment fragment = FragmentFactory.create(entry.identity());
        fragment.setArguments(bundle);

        fragmentManager().beginTransaction()
                .replace(R.id.frame, fragment)
                .commitAllowingStateLoss();
    }

    /* ======================= HELPERS ======================= */

    private FragmentManager fragmentManager() {
        return activity.getSupportFragmentManager();
    }

    private boolean isRoot(String identity) {
        return getRootFragments().contains(identity);
    }

    public List<String> getRootFragments() {
        return Stream.of(
                OrdersFragment.IDENTITY,
                GoodsFragment.IDENTITY,
                ClientsFragment.IDENTITY,
                DeliveriesFragment.IDENTITY
        ).collect(Collectors.toList());
    }

    /* ======================= DATA ======================= */


    public boolean callbackBack() {
        NavEntry last = backStack.getLast();
        if (onBackListeners.isEmpty() || onBackListeners.getLast().onBack(last)) {
            back();
        }
        return true;
    }

    public void addOnBackListener(OnBackListener onBackListener) {
        this.onBackListeners.add(onBackListener);
    }

    public void removeOnBackListener(OnBackListener onBackListener) {
        this.onBackListeners.remove(onBackListener);
    }

    public interface OnBackListener {
        boolean onBack(NavEntry fragment);
    }
}
