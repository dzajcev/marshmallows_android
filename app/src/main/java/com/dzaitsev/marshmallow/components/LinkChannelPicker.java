package com.dzaitsev.marshmallow.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.dzaitsev.marshmallow.dto.LinkChannel;

import java.util.function.BiConsumer;

public class LinkChannelPicker extends View {
    private final AlertDialog.Builder builder;

    private AlertDialog alertDialog;
    final LinkChannelSelector linkChannelSelector;

    private DialogInterface.OnShowListener onShowListener;

    public LinkChannelPicker(Context context) {
        super(context);
        this.builder = new AlertDialog.Builder(context);
        this.linkChannelSelector = new LinkChannelSelector(context, 105, 105, LinkChannelSelectorComponent.Mode.BUTTON);
        builder.setNeutralButton("Закрыть", (dialog, which) -> {
//do nothing
        });
        builder.setView(linkChannelSelector);


    }

    public static Builder builder(Context context) {
        return new LinkChannelPicker(context).new Builder();
    }

    public void show() {
        alertDialog = builder.create();
        alertDialog.setOnShowListener(onShowListener);
        alertDialog.show();
    }

    public class Builder {

        public Builder setTitle(String title) {
            LinkChannelPicker.this.builder.setTitle(title);
            return this;
        }


        public Builder setAction(BiConsumer<AlertDialog, LinkChannel> consumer) {
            linkChannelSelector.setOnClickLinkChannelListener((linkChannelSelector, linkChannelSelectorComponent, linkChannel)
                    -> consumer.accept(LinkChannelPicker.this.alertDialog, linkChannel));
            return this;
        }

        public LinkChannelPicker build() {
           return LinkChannelPicker.this;
        }
    }
}
