package com.dzaitsev.marshmallow.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.utils.MoneyUtils;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MoneyPicker extends View {
    private final AlertDialog.Builder builder;
    private final EditText money;

    private Integer minValue;
    private Integer maxValue;

    private DialogInterface.OnShowListener onShowListener;
    private View.OnClickListener positiveButton;

    private AlertDialog alertDialog;


    public MoneyPicker(Context context) {
        super(context);
        this.builder = new AlertDialog.Builder(context);
        this.money = new EditText(context);
        this.money.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        this.money.setGravity(Gravity.CENTER);
        this.money.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        this.money.setBackgroundColor(ContextCompat.getColor(context, R.color.field_background));

        builder.setNegativeButton("CANCEL", (dialog, which) -> {
//do nothing
        });
        builder.setPositiveButton("Ok", null);
        builder.setView(money);
    }

    public static Builder builder(Context context) {
        return new MoneyPicker(context).new Builder();
    }

    public void show() {
        alertDialog = builder.create();
        alertDialog.setOnShowListener(onShowListener);
        alertDialog.show();
        Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        button.setOnClickListener(positiveButton);
        money.requestFocus();
    }

    public class Builder {

        public Builder setTitle(String title) {
            MoneyPicker.this.builder.setTitle(title);
            return this;
        }

        public Builder setMessage(String message) {
            MoneyPicker.this.builder.setMessage(message);
            return this;
        }

        public Builder setMinValue(Integer minValue) {
            MoneyPicker.this.minValue = minValue;
            return this;
        }

        public Builder setMaxValue(Integer maxValue) {
            MoneyPicker.this.maxValue = maxValue;
            return this;
        }

        public Builder setInitialValue(Double maxValue) {
            MoneyPicker.this.money.setText(MoneyUtils.moneyToString(maxValue));
            return this;
        }

        public Builder positiveButton(Consumer<Double> supplier) {
            positiveButton = view -> {
                Optional.ofNullable(MoneyUtils.stringToDouble(money.getText().toString())).ifPresent(val -> {
                    boolean badValue = minValue != null && val < minValue;
                    if (maxValue != null && val > maxValue) {
                        badValue = true;
                    }
                    if (badValue) {
                        String intervalMessage;
                        if (minValue != null && maxValue == null) {
                            intervalMessage = "Значение должно быть больше или равно " + minValue;
                        } else if (minValue == null) {
                            intervalMessage = "Значение должно быть меньше или равно " + maxValue;
                        } else {
                            intervalMessage = "Значение должно быть меньше или равно " + maxValue + " и больше или равно " + minValue;
                        }
                        Toast.makeText(money.getContext(), String.format(intervalMessage, MoneyPicker.this.minValue, MoneyPicker.this.maxValue), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    supplier.accept(val);
                });
                MoneyPicker.this.alertDialog.dismiss();
            };

            return this;
        }

        public Builder dialogShowListener(BiConsumer<DialogInterface, EditText> consumer) {
            onShowListener = dialogInterface -> consumer.accept(dialogInterface, money);
            return this;
        }

        public MoneyPicker build() {
            return MoneyPicker.this;
        }
    }
}
