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
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dzaitsev.marshmallow.databinding.ActivityMainBinding;
import com.dzaitsev.marshmallow.dto.Order;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    private NavigationUpListener navigationBackListener;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.ordersFragment, R.id.orderCardFragment).build();

        //, R.id.clientsFragment, R.id.goodsFragment,
        //                R.id.orderGoodsFragment, R.id.orderClientFragment
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {
                        if (navDestination.getId() == R.id.ordersFragment
                                || (navDestination.getId() == R.id.goodsFragment
                                && (bundle==null || bundle.getSerializable("order", Order.class) == null))
                                || (bundle==null || bundle.getSerializable("order", Order.class) == null)
                                && navDestination.getId() == R.id.clientsFragment) {
                            bottomNavigationView.setVisibility(View.VISIBLE);

                        } else {
                            bottomNavigationView.setVisibility(View.GONE);
                        }
                }
        );
        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                }
        );
        requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optiona_menu, menu);

        return true;

    }

    public void setNavigationBackListener(NavigationUpListener navigationBackListener) {
        this.navigationBackListener = navigationBackListener;
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navigationBackListener != null) {
            if (!navigationBackListener.accept()) {
                return false;
            }
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}