package com.dzaitsev.marshmallow;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.dzaitsev.marshmallow.fragments.ClientsFragment;
import com.dzaitsev.marshmallow.fragments.DeliveriesFragment;
import com.dzaitsev.marshmallow.fragments.GoodsFragment;
import com.dzaitsev.marshmallow.fragments.IdentityFragment;
import com.dzaitsev.marshmallow.fragments.OrdersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {

                    });

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof IdentityFragment identityFragment) {
                if ((Navigation.getNavigation(MainActivity.this).getRootFragments().stream()
                        .anyMatch(a -> a.equals(identityFragment.getUniqueName())))
                        && (fragment.getArguments() == null || fragment.getArguments().isEmpty())) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                } else {
                    bottomNavigationView.setVisibility(View.GONE);
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
            }else if (item.getItemId() == R.id.deliveryMenu) {
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
        Navigation.getNavigation(this).goForward(new OrdersFragment(), new Bundle());
    }

    private AlertDialog alertDialog;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setMessage("Some permissions are need to be allowed to use this app without any problems.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    dialog.dismiss();
                });
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
        /* User has pressed 'Deny & Don't ask again' so we have to show the enable permissions dialog
        which will lead them to app details page to enable permissions from there. */
            showPermissionDialog();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optiona_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Navigation.getNavigation(this).callbackBack();
    }
}