package com.dzaitsev.marshmallow.utils;

import android.widget.EditText;

public class EditTextUtil {

    public static void setText(EditText editText, String text) {
        editText.post(() -> editText.setText(text));

    }
}
