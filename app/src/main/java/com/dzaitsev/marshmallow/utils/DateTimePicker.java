package com.dzaitsev.marshmallow.utils;

import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Locale;
import java.util.function.Consumer;

public class DateTimePicker extends View {
    private final Calendar calendar = Calendar.getInstance(new Locale("RU", "ru"));
    private final DatePicker datePicker;
    private final android.app.AlertDialog alertDialog;

    public DateTimePicker(Context context, Consumer<LocalDate> dateConsumer, String title, String message) {
        super(context);

        Locale.setDefault(new Locale("RU", "ru"));
        datePicker = new DatePicker(context);
        datePicker.setMinDate(System.currentTimeMillis());
        datePicker.setFirstDayOfWeek(Calendar.MONDAY);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);


        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", (dialog, which) -> {

            calendar.set(Calendar.YEAR, datePicker.getYear());
            calendar.set(Calendar.MONTH, datePicker.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
            dateConsumer.accept(LocalDate.of(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth()));
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