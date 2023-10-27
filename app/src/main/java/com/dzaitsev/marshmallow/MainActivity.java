package com.dzaitsev.marshmallow;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.dto.DeliveryFilter;
import com.dzaitsev.marshmallow.dto.DeliveryStatus;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.dto.OrdersFilter;
import com.dzaitsev.marshmallow.dto.authorization.request.SignUpRequest;
import com.dzaitsev.marshmallow.fragments.ClientsFragment;
import com.dzaitsev.marshmallow.fragments.ConfirmRegistrationFragment;
import com.dzaitsev.marshmallow.fragments.DeliveriesFragment;
import com.dzaitsev.marshmallow.fragments.GoodsFragment;
import com.dzaitsev.marshmallow.fragments.IdentityFragment;
import com.dzaitsev.marshmallow.fragments.LoginFragment;
import com.dzaitsev.marshmallow.fragments.OrdersFragment;
import com.dzaitsev.marshmallow.fragments.UserCardFragment;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelper;
import com.dzaitsev.marshmallow.utils.authorization.AuthorizationHelperInitializer;
import com.dzaitsev.marshmallow.utils.navigation.Navigation;
import com.dzaitsev.marshmallow.utils.navigation.NavigationInitializer;
import com.dzaitsev.marshmallow.utils.network.NetworkExecutorHelper;
import com.dzaitsev.marshmallow.utils.orderfilter.FiltersHelper;
import com.dzaitsev.marshmallow.utils.orderfilter.FiltersHelperInitializer;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {

                    });

    private void processDeliveryFilter() {
        FiltersHelper.getInstance().getDeliveryFilter()
                .or(() -> {
                    DeliveryFilter deliveryFilter = new DeliveryFilter();
                    deliveryFilter.getStatuses().add(DeliveryStatus.IN_PROGRESS);
                    deliveryFilter.getStatuses().add(DeliveryStatus.DONE);
                    deliveryFilter.getStatuses().add(DeliveryStatus.NEW);
                    return Optional.of(deliveryFilter);
                }).map(deliveryFilter -> {
                    deliveryFilter.setStart(LocalDate.now().minusWeeks(1));
                    deliveryFilter.setEnd(LocalDate.now().plusWeeks(1));
                    return deliveryFilter;
                }).ifPresent(deliveryFilter -> FiltersHelper.getInstance().updateDeliveryFilter(deliveryFilter));
    }

    private void processOrderFilter() {
        FiltersHelper.getInstance().getOrderFilter()
                .or(() -> {
                    OrdersFilter ordersFilter = new OrdersFilter();
                    ordersFilter.getStatuses().add(OrderStatus.IN_PROGRESS);
                    ordersFilter.getStatuses().add(OrderStatus.SHIPPED);
                    ordersFilter.getStatuses().add(OrderStatus.DONE);
                    ordersFilter.getStatuses().add(OrderStatus.IN_DELIVERY);
                    return Optional.of(ordersFilter);
                }).map(ordersFilter -> {
                    ordersFilter.setStart(LocalDate.now().minusWeeks(1));
                    ordersFilter.setEnd(LocalDate.now().plusWeeks(1));
                    return ordersFilter;
                }).ifPresent(ordersFilter -> FiltersHelper.getInstance().updateOrderFilter(ordersFilter));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        AuthorizationHelperInitializer.init(preferences);
        FiltersHelperInitializer.init(preferences);
        NavigationInitializer.init(this, bottomNavigationView);
        processDeliveryFilter();
        processOrderFilter();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View userCard = toolbar.findViewById(R.id.userCard);
        userCard.setOnClickListener(v -> Navigation.getNavigation().goForward(new UserCardFragment()));

        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof IdentityFragment identityFragment) {
                if ((Navigation.getNavigation().getRootFragments().stream()
                        .anyMatch(a -> a.equals(identityFragment.getUniqueName())))
                        && (fragment.getArguments() == null || fragment.getArguments().isEmpty())) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    userCard.setVisibility(View.VISIBLE);
                } else {
                    bottomNavigationView.setVisibility(View.GONE);
                    userCard.setVisibility(View.GONE);
                }
            }
        });

        NetworkExecutorHelper.setGlobalErrorListener(code -> {
            switch (code) {
                case AUTH006 -> {//обновление токена
                }
                case AUTH008 -> {
                    NetworkService.getInstance().refreshToken(null);
                    AuthorizationHelper.getInstance().getSignInRequest()
                            .ifPresent(r -> new NetworkExecutorHelper<>(MainActivity.this, NetworkService.getInstance().getAuthorizationApi()
                                    .signUp(new SignUpRequest(r.getEmail(), r.getPassword())))
                                    .invoke(response -> Optional.ofNullable(response.body())
                                            .ifPresent(jwtSignUpResponse -> {
                                                Bundle bundle = new Bundle();
                                                bundle.putInt("ttlCode", jwtSignUpResponse.getVerificationCodeTtl());
                                                bundle.putString("token", jwtSignUpResponse.getToken());
                                                bundle.putString("login", r.getEmail());
                                                bundle.putString("password", r.getPassword());
                                                Navigation.getNavigation().goForward(new ConfirmRegistrationFragment(), bundle);
                                            })));
                }
                default -> {
                    Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.frame);
                    Toast.makeText(this, code.getText(), Toast.LENGTH_SHORT).show();
                    if (!(fragmentById instanceof LoginFragment)) {
                        AuthorizationHelper.getInstance().updateSignInRequest(null);
                        Navigation.getNavigation().goForward(new LoginFragment());
                    }
                }
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.frame);
            if (item.getItemId() == R.id.ordersMenu) {
                if (!(fragmentById instanceof OrdersFragment)) {
                    Navigation.getNavigation().goForward(new OrdersFragment());
                    return true;
                }

            } else if (item.getItemId() == R.id.goodsMenu) {
                if (!(fragmentById instanceof GoodsFragment)) {
                    Navigation.getNavigation().goForward(new GoodsFragment());
                    return true;
                }
            } else if (item.getItemId() == R.id.clientsMenu) {
                if (!(fragmentById instanceof ClientsFragment)) {
                    Navigation.getNavigation().goForward(new ClientsFragment());
                    return true;
                }
            } else if (item.getItemId() == R.id.deliveryMenu) {
                if (!(fragmentById instanceof DeliveriesFragment)) {
                    Navigation.getNavigation().goForward(new DeliveriesFragment());
                    return true;
                }
            }
            return false;
        });
        List<String> permissionsList = new ArrayList<>(Arrays.asList(Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.SEND_SMS));
        askForPermissions(permissionsList);
        AuthorizationHelper.getInstance().getSignInRequest()
                .ifPresentOrElse(signInRequest -> NetworkExecutorHelper.authorize(MainActivity.this, signInRequest),
                        () -> Navigation.getNavigation().goForward(new LoginFragment(), new Bundle()));
    }

    private AlertDialog alertDialog;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setMessage("Some permissions are need to be allowed to use this app without any problems.")
                .setPositiveButton("Settings", (dialog, which) -> dialog.dismiss());
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private void askForPermissions(List<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);
        } else {
            showPermissionDialog();
        }
    }

    @Override
    public void onBackPressed() {
        Navigation.getNavigation().callbackBack();
    }
}