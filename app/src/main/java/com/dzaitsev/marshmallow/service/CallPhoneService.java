package com.dzaitsev.marshmallow.service;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class CallPhoneService {
    private static CallPhoneService callPhoneService;

    private CallPhoneService() {
    }

    public static CallPhoneService getInstance() {
        if (callPhoneService == null) {
            callPhoneService = new CallPhoneService();
        }
        return callPhoneService;
    }


    public void call(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Нет разрешения на звонки", Toast.LENGTH_SHORT).show();
        }
    }
}
