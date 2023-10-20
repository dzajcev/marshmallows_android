package com.dzaitsev.marshmallow.components;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Locale;
import java.util.function.Consumer;

public class DatePicker extends View {
    private final android.widget.DatePicker datePicker;
    private final android.app.AlertDialog alertDialog;

    public DatePicker(Context context, Consumer<LocalDate> dateConsumer, String title, String message) {
        super(context);

        Locale.setDefault(new Locale("RU", "ru"));
        datePicker = new android.widget.DatePicker(context);
        datePicker.setMinDate(System.currentTimeMillis());
        datePicker.setFirstDayOfWeek(Calendar.MONDAY);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);


        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", (dialog, which) -> {
            dateConsumer.accept(LocalDate.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> {

        });
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(datePicker);
        builder.setView(linearLayout);
        alertDialog = builder.create();


    }

    public void show() {
        alertDialog.show();
    }

}