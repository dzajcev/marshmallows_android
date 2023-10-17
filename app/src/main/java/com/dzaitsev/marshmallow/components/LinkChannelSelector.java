package com.dzaitsev.marshmallow.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.LinkChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LinkChannelSelector extends ConstraintLayout {
    LinkChannelSelectorComponent.OnCheckedChangeListener onCheckedChangeListener;

    private LinkChannelSelectorComponent phone;
    private LinkChannelSelectorComponent sms;
    private LinkChannelSelectorComponent whatsapp;
    private LinkChannelSelectorComponent telegram;

    final Map<LinkChannel, LinkChannelSelectorComponent> channels = new HashMap<>();


    @RequiresApi(api = Build.VERSION_CODES.S)
    public LinkChannelSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

//        performClick();
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinkChannelSelector, 0, 0)) {
            int iconHeight = a.getDimensionPixelSize(R.styleable.LinkChannelSelector_iconHeight, 0);
            int iconWidth = a.getDimensionPixelSize(R.styleable.LinkChannelSelector_iconWidth, 0);
            int mode = a.getInt(R.styleable.LinkChannelSelector_mode, 0);
            initControl(context, LinkChannelSelectorComponent.Mode.values()[mode]);
            phone.setDimensions(iconWidth, iconHeight);
            sms.setDimensions(iconWidth, iconHeight);
            whatsapp.setDimensions(iconWidth, iconHeight);
            telegram.setDimensions(iconWidth, iconHeight);
        }

    }

    @Override
    public void onViewAdded(View view) {
        super.onViewAdded(view);
    }

    private void initControl(Context context, LinkChannelSelectorComponent.Mode mode) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.link_channel_selector, this);
        phone = findViewById(R.id.phone);
        phone.setMode(mode);
        sms = findViewById(R.id.sms);
        sms.setMode(mode);
        whatsapp = findViewById(R.id.whatsapp);
        whatsapp.setMode(mode);
        telegram = findViewById(R.id.telegram);
        telegram.setMode(mode);

        channels.put(phone.getLinkChannel(), phone);
        channels.put(sms.getLinkChannel(), sms);
        channels.put(whatsapp.getLinkChannel(), whatsapp);
        channels.put(telegram.getLinkChannel(), telegram);
    }

    public boolean isEmpty() {
        return getSelectedChannels().isEmpty();
    }

    public List<LinkChannel> getSelectedChannels() {
        return Stream.of(phone, sms, whatsapp, telegram)
                .filter(LinkChannelSelectorComponent::isChecked)
                .map(LinkChannelSelectorComponent::getLinkChannel)
                .collect(Collectors.toList());

    }

    public void restoreBackgroundColor(){

    }
    public void setChecked(List<LinkChannel> linkChannels) {
        linkChannels.forEach(l -> {
            Optional.ofNullable(channels.get(l)).ifPresent(linkChannelSelectorComponent -> linkChannelSelectorComponent.setChecked(true));
        });
    }


    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener::onCheckedChanged;
        phone.setOnCheckedChangeListener(this.onCheckedChangeListener);
        sms.setOnCheckedChangeListener(this.onCheckedChangeListener);
        whatsapp.setOnCheckedChangeListener(this.onCheckedChangeListener);
        telegram.setOnCheckedChangeListener(this.onCheckedChangeListener);

    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(LinkChannelSelectorComponent linkChannelSelectorComponent, boolean isChecked);
    }

}
