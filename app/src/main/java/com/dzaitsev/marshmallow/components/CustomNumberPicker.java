package com.dzaitsev.marshmallow.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.NumberPicker;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CustomNumberPicker extends View {
    private final AlertDialog.Builder builder;
    final NumberPicker numberPicker;

    private DialogInterface.OnShowListener onShowListener;

    public CustomNumberPicker(Context context) {
        super(context);
        this.builder = new AlertDialog.Builder(context);
        this.numberPicker = new NumberPicker(context);
        this.numberPicker.setWrapSelectorWheel(false);
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
//do nothing
        });
        builder.setView(numberPicker);


    }

    public static Builder builder(Context context) {
        return new CustomNumberPicker(context).new Builder();
    }

    public void show(){
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(onShowListener);
        alertDialog.show();
    }

    public class Builder {

        public Builder setTitle(String title) {
            CustomNumberPicker.this.builder.setTitle(title);
            return this;
        }
        public Builder setInitialValue(Integer initialValue) {
            CustomNumberPicker.this.numberPicker.setValue(initialValue);
            return this;
        }
        public Builder setMinValue(Integer minValue) {
            CustomNumberPicker.this.numberPicker.setMinValue(minValue);
            return this;
        }

        public Builder setMaxValue(Integer maxValue) {
            CustomNumberPicker.this.numberPicker.setMaxValue(maxValue);
            return this;
        }

        public Builder positiveButton(Consumer<Integer> supplier) {
            CustomNumberPicker.this.builder.setPositiveButton("Ok",
                    (dialogInterface, i) -> supplier.accept(numberPicker.getValue()));
            return this;
        }

        public Builder dialogShowListener(BiConsumer<DialogInterface, NumberPicker> consumer) {
            onShowListener = dialogInterface -> consumer.accept(dialogInterface, numberPicker);
            return this;
        }

        public CustomNumberPicker build() {
            return CustomNumberPicker.this;
        }
    }
}
