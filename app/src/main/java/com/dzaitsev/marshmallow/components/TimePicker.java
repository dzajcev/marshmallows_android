package com.dzaitsev.marshmallow.components;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import java.time.LocalTime;
import java.util.Locale;
import java.util.function.Consumer;

public class TimePicker extends View {

    private final android.widget.TimePicker timePicker;
    private final AlertDialog alertDialog;

    public TimePicker(Context context, Consumer<LocalTime> timeConsumer, String title, String message) {
        super(context);

        Locale.setDefault(new Locale("RU", "ru"));
        timePicker = new android.widget.TimePicker(context);
        timePicker.setIs24HourView(true);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);


        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", (dialog, which) -> {
            timeConsumer.accept(LocalTime.of(timePicker.getHour(), timePicker.getMinute()));
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> {

        });
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(timePicker);
        builder.setView(linearLayout);
        alertDialog = builder.create();
    }

    public void show() {
        alertDialog.show();
    }

}