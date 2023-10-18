package com.dzaitsev.marshmallow;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.dzaitsev.marshmallow.fragments.OrdersFragment;

public class MainActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                }
        );
        requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        Navigation.getNavigation(this).goForward(new OrdersFragment(), new Bundle());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.frame, new OrdersFragment());
        fragmentTransaction.commit();
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