package com.dzaitsev.marshmallow.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class StringUtils {
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static class ErrorDialog {
        AlertDialog dialog;

        public ErrorDialog(@NonNull Context activity, String message) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Ошибка выполнения");
            builder.setMessage(message);
            builder.setNeutralButton("OK", (dialog, which) -> {
            });
            dialog = builder.create();
        }

        public void show() {
            dialog.show();
        }
    }



}
