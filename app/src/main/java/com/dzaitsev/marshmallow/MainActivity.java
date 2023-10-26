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
import com.dzaitsev.marshmallow.dto.authorization.response.JwtAuthenticationResponse;
import com.dzaitsev.marshmallow.dto.request.SignInRequest;
import com.dzaitsev.marshmallow.dto.request.SignUpRequest;
import com.dzaitsev.marshmallow.fragments.ClientsFragment;
import com.dzaitsev.marshmallow.fragments.ConfirmRegistrationFragment;
import com.dzaitsev.marshmallow.fragments.DeliveriesFragment;
import com.dzaitsev.marshmallow.fragments.GoodsFragment;
import com.dzaitsev.marshmallow.fragments.IdentityFragment;
import com.dzaitsev.marshmallow.fragments.LoginFragment;
import com.dzaitsev.marshmallow.fragments.OrdersFragment;
import com.dzaitsev.marshmallow.fragments.UserCardFragment;
import com.dzaitsev.marshmallow.service.NetworkExecutorWrapper;
import com.dzaitsev.marshmallow.service.NetworkService;
import com.dzaitsev.marshmallow.utils.GsonExt;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {

                    });

    private void processDeliveryFilter() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        String string = preferences.getString("delivery-filter", "");
        Gson gson = GsonExt.getGson();
        DeliveryFilter deliveryFilter = gson.fromJson(string, DeliveryFilter.class);
        if (deliveryFilter == null) {
            deliveryFilter = new DeliveryFilter();
            deliveryFilter.getStatuses().add(DeliveryStatus.IN_PROGRESS);
            deliveryFilter.getStatuses().add(DeliveryStatus.DONE);
            deliveryFilter.getStatuses().add(DeliveryStatus.NEW);

        }
        deliveryFilter.setStart(LocalDate.now().minusWeeks(1));
        deliveryFilter.setEnd(LocalDate.now().plusWeeks(1));
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("delivery-filter", GsonExt.getGson().toJson(deliveryFilter));
        edit.apply();
    }

    private void processOrderFilter() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        String string = preferences.getString("order-filter", "");
        Gson gson = GsonExt.getGson();
        OrdersFilter ordersFilter = gson.fromJson(string, OrdersFilter.class);
        if (ordersFilter == null) {
            ordersFilter = new OrdersFilter();
            ordersFilter.getStatuses().add(OrderStatus.IN_PROGRESS);
            ordersFilter.getStatuses().add(OrderStatus.SHIPPED);
            ordersFilter.getStatuses().add(OrderStatus.DONE);
            ordersFilter.getStatuses().add(OrderStatus.IN_DELIVERY);

        }
        ordersFilter.setStart(LocalDate.now().minusWeeks(1));
        ordersFilter.setEnd(LocalDate.now().plusWeeks(1));
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("order-filter", GsonExt.getGson().toJson(ordersFilter));
        edit.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processDeliveryFilter();
        processOrderFilter();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View userCard = toolbar.findViewById(R.id.userCard);
        userCard.setOnClickListener(v -> Navigation.getNavigation(MainActivity.this).goForward(new UserCardFragment()));
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof IdentityFragment identityFragment) {
                if ((Navigation.getNavigation(MainActivity.this).getRootFragments().stream()
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

        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
        String string = preferences.getString("authorization-data", "");
        Gson gson = GsonExt.getGson();
        SignInRequest request = gson.fromJson(string, SignInRequest.class);
        NetworkExecutorWrapper.setGlobalErrorListener(code -> {
            switch (code) {
                case AUTH006 -> {//обновление токена
                }
                case AUTH008 -> {
                    NetworkService.getInstance().refreshToken(null);
                    new NetworkExecutorWrapper<>(MainActivity.this, NetworkService.getInstance().getAuthorizationApi()
                            .signUp(new SignUpRequest(request.getEmail(), request.getPassword())))
                            .invoke(response -> Optional.ofNullable(response.body())
                                    .ifPresent(jwtSignUpResponse -> {
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("ttCode", jwtSignUpResponse.getVerificationCodeTtl());
                                        bundle.putString("token", jwtSignUpResponse.getToken());
                                        bundle.putString("login", request.getEmail());
                                        bundle.putString("password", request.getPassword());
                                        Navigation.getNavigation(MainActivity.this).goForward(new ConfirmRegistrationFragment(), bundle);
                                    }));
                }
                default -> {
                    Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.frame);
                    Toast.makeText(this, code.getText(), Toast.LENGTH_SHORT).show();
                    if (!(fragmentById instanceof LoginFragment)) {
                        Navigation.getNavigation(MainActivity.this).goForward(new LoginFragment());
                    }
                }
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.frame);
            if (item.getItemId() == R.id.ordersMenu) {
                if (!(fragmentById instanceof OrdersFragment)) {
                    Navigation.getNavigation(MainActivity.this).goForward(new OrdersFragment());
                    return true;
                }

            } else if (item.getItemId() == R.id.goodsMenu) {
                if (!(fragmentById instanceof GoodsFragment)) {
                    Navigation.getNavigation(MainActivity.this).goForward(new GoodsFragment());
                    return true;
                }
            } else if (item.getItemId() == R.id.clientsMenu) {
                if (!(fragmentById instanceof ClientsFragment)) {
                    Navigation.getNavigation(MainActivity.this).goForward(new ClientsFragment());
                    return true;
                }
            } else if (item.getItemId() == R.id.deliveryMenu) {
                if (!(fragmentById instanceof DeliveriesFragment)) {
                    Navigation.getNavigation(MainActivity.this).goForward(new DeliveriesFragment());
                    return true;
                }
            }
            return false;
        });
        List<String> permissionsList = new ArrayList<>(Arrays.asList(Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.SEND_SMS));
        askForPermissions(permissionsList);
        if (request != null) {
            authorize(request);
        } else {
            Navigation.getNavigation(this).goForward(new LoginFragment(), new Bundle());
        }
    }

    private void authorize(SignInRequest signInRequest) {
        new NetworkExecutorWrapper<>(this, NetworkService.getInstance().getAuthorizationApi().signIn(signInRequest))
                .invoke(response -> {
                    if (response.isSuccessful()) {
                        Optional.ofNullable(response.body())
                                .map(JwtAuthenticationResponse::getToken)
                                .ifPresent(s -> {
                                    NetworkService.getInstance().refreshToken(s);
                                    Navigation.getNavigation(MainActivity.this).goForward(new OrdersFragment());
                                });
                    }
//                    else {
//                        if (response.code() == 403) {
//                            MainActivity.this.runOnUiThread(() -> Toast.makeText(MainActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show());
//                        } else {
//                            Toast.makeText(MainActivity.this, "Авторизация не удалась", Toast.LENGTH_SHORT).show();
//                        }
//                        Navigation.getNavigation(MainActivity.this).goForward(new LoginFragment(), new Bundle());
//                    }
                });
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
        Navigation.getNavigation(this).callbackBack();
    }
}