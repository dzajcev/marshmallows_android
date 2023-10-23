package com.dzaitsev.marshmallow.components;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.dzaitsev.marshmallow.dto.OrderLine;

import java.util.List;

public class OrderSimpleDialog extends View {
    private final AlertDialog.Builder builder;

    final OrderSimpleComponent orderSimpleComponent;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public OrderSimpleDialog(Context context) {
        super(context);
        this.builder = new AlertDialog.Builder(context);
        this.orderSimpleComponent = new OrderSimpleComponent(context);
        builder.setNeutralButton("Закрыть", (dialog, which) -> {
//do nothing
        });
        builder.setView(orderSimpleComponent);


    }

    public static Builder builder(Context context) {
        return new OrderSimpleDialog(context).new Builder();
    }

    public void show() {
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public class Builder {
        public Builder setTitle(String title) {
            OrderSimpleDialog.this.builder.setTitle(title);
            return this;
        }

        public Builder setItems(List<OrderLine> items) {
            OrderSimpleDialog.this.orderSimpleComponent.setItems(items);
            return this;
        }

        public OrderSimpleDialog build() {
            return OrderSimpleDialog.this;
        }
    }
}
