package com.dzaitsev.marshmallow;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.dto.DeliveryFilter;
import com.dzaitsev.marshmallow.dto.DeliveryStatus;
import com.dzaitsev.marshmallow.dto.ErrorCodes;
import com.dzaitsev.marshmallow.dto.OrderStatus;
import com.dzaitsev.marshmallow.dto.OrdersFilter;
import com.dzaitsev.marshmallow.dto.authorization.request.SignInRequest;
import com.dzaitsev.marshmallow.dto.authorization.request.SignUpRequest;
import com.dzaitsev.marshmallow.fragments.ConfirmRegistrationFragment;
import com.dzaitsev.marshmallow.fragments.IdentityFragment;
import com.dzaitsev.marshmallow.fragments.LoginFragment;
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
        DeliveryFilter deliveryFilter;
        if (FiltersHelper.getInstance().getDeliveryFilter().isPresent()) {
            deliveryFilter = FiltersHelper.getInstance().getDeliveryFilter().get();
        } else {
            deliveryFilter = new DeliveryFilter();
            deliveryFilter.getStatuses().addAll(Arrays.asList(DeliveryStatus.values()));
        }
        deliveryFilter.setStart(LocalDate.now().minusWeeks(1));
        deliveryFilter.setEnd(LocalDate.now().plusWeeks(1));
        FiltersHelper.getInstance().updateDeliveryFilter(deliveryFilter);
    }

    private void processOrderFilter() {
        OrdersFilter ordersFilter;
        if (FiltersHelper.getInstance().getOrderFilter().isPresent()) {
            ordersFilter = FiltersHelper.getInstance().getOrderFilter().get();
        } else {
            ordersFilter = new OrdersFilter();
            ordersFilter.getStatuses().addAll(Arrays.asList(OrderStatus.values()));
        }
        ordersFilter.setStart(LocalDate.now().minusWeeks(1));
        ordersFilter.setEnd(LocalDate.now().plusWeeks(1));
        FiltersHelper.getInstance().updateOrderFilter(ordersFilter);
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
//
//        NetworkExecutorHelper.setAuthorizeListener(user -> {
//            if (user.getRole()== UserRole.DELIVERYMAN){
//                bottomNavigationView.setVisibility(View.GONE);
//            }
//        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View userCard = toolbar.findViewById(R.id.userCard);
        userCard.setOnClickListener(v -> Navigation.getNavigation().forward( UserCardFragment.IDENTITY));

        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof IdentityFragment identityFragment) {
                if ((Navigation.getNavigation().getRootFragments().stream()
                        .anyMatch(a -> a.equals(identityFragment.getUniqueName())))
                        && (fragment.getArguments() == null || fragment.getArguments().isEmpty())) {
                    userCard.setVisibility(View.VISIBLE);
                } else {
                    userCard.setVisibility(View.GONE);
                }
            }
        });
        NetworkExecutorHelper.setGlobalErrorListener(new NetworkExecutorHelper.OnErrorListener() {
            @Override
            public void onError(ErrorCodes code, String text) {
                switch (code) {
                    case AUTH006 -> {//обновление токена
                    }
                    case AUTH001 -> //код неверный
                            Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
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
                                                    Navigation.getNavigation().forward( ConfirmRegistrationFragment.IDENTITY, bundle);
                                                })));
                    }
                    default -> {
                        Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.frame);
                        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
                        if (!(fragmentById instanceof LoginFragment)) {
                            AuthorizationHelper.getInstance().updateSignInRequest(null);
                            Navigation.getNavigation().forward(LoginFragment.IDENTITY);
                        }
                    }
                }
            }
        });


        List<String> permissionsList = new ArrayList<>(Arrays.asList(Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.SEND_SMS));
        askForPermissions(permissionsList);
        Optional<SignInRequest> request = AuthorizationHelper.getInstance().getSignInRequest();
        if (request.isPresent()) {
            NetworkExecutorHelper.authorize(MainActivity.this, request.get());
        } else {
            Navigation.getNavigation().forward( LoginFragment.IDENTITY, new Bundle());
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame);
                if (fragment instanceof IdentityFragment identityFragment) {
                    boolean handled = Navigation.getNavigation().callbackBack();
                    if (!handled) {
                        // Если фрагмент не обработал back — вызываем стандартное поведение
                        setEnabled(false);
                        onBackPressed();
                        setEnabled(true);
                    }
                } else {
                    // Нет активного IdentityFragment — стандартный back
                    setEnabled(false);
                    onBackPressed();
                    setEnabled(true);
                }
            }
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
}