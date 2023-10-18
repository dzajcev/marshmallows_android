package com.dzaitsev.marshmallow.components;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.LinkChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LinkChannelSelector extends ConstraintLayout {

    private LinkChannelSelectorComponent phone;
    private LinkChannelSelectorComponent sms;
    private LinkChannelSelectorComponent whatsapp;
    final Map<LinkChannel, LinkChannelSelectorComponent> channels = new HashMap<>();


    @RequiresApi(api = Build.VERSION_CODES.S)
    public LinkChannelSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinkChannelSelector, 0, 0)) {
            int iconHeight = a.getDimensionPixelSize(R.styleable.LinkChannelSelector_iconHeight, 0);
            int iconWidth = a.getDimensionPixelSize(R.styleable.LinkChannelSelector_iconWidth, 0);
            int mode = a.getInt(R.styleable.LinkChannelSelector_mode, 0);
            initControl(context, LinkChannelSelectorComponent.Mode.values()[mode]);
            phone.setDimensions(iconWidth, iconHeight);
            sms.setDimensions(iconWidth, iconHeight);
            whatsapp.setDimensions(iconWidth, iconHeight);
        }
    }

    public LinkChannelSelector(@NonNull Context context, int iconHeight, int iconWidth, LinkChannelSelectorComponent.Mode mode) {
        super(context);
        initControl(context, mode);
        phone.setDimensions(iconWidth, iconHeight);
        sms.setDimensions(iconWidth, iconHeight);
        whatsapp.setDimensions(iconWidth, iconHeight);
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
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            phone.setVisibility(GONE);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            sms.setVisibility(GONE);
        }
        if (!isAppInstalled("com.whatsapp")) {
            whatsapp.setVisibility(GONE);
        }

        channels.put(phone.getLinkChannel(), phone);
        channels.put(sms.getLinkChannel(), sms);
        channels.put(whatsapp.getLinkChannel(), whatsapp);
    }

    public boolean isEmpty() {
        return getSelectedChannels().isEmpty();
    }

    public List<LinkChannel> getSelectedChannels() {
        return Stream.of(phone, sms, whatsapp)
                .filter(LinkChannelSelectorComponent::isChecked)
                .map(LinkChannelSelectorComponent::getLinkChannel)
                .collect(Collectors.toList());

    }

    public void setChecked(List<LinkChannel> linkChannels) {
        linkChannels.forEach(l -> Optional.ofNullable(channels.get(l)).ifPresent(linkChannelSelectorComponent
                -> linkChannelSelectorComponent.setChecked(true)));
    }


    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        LinkChannelSelectorComponent.OnCheckedChangeListener l
                = (linkChannelSelectorComponent, isChecked)
                -> onCheckedChangeListener.onCheckedChanged(LinkChannelSelector.this, getSelectedChannels());
        phone.setOnCheckedChangeListener(l);
        sms.setOnCheckedChangeListener(l);
        whatsapp.setOnCheckedChangeListener(l);
    }

    public void setOnClickLinkChannelListener(OnClickLinkChannelListener onClickLinkChannelListener) {
        LinkChannelSelectorComponent.OnButtonClickListener onButtonClickListener
                = (linkChannelSelectorComponent, linkChannel)
                -> onClickLinkChannelListener.onClickLinkChannel(LinkChannelSelector.this,
                linkChannelSelectorComponent, linkChannel);
        phone.setOnButtonClickListener(onButtonClickListener);
        sms.setOnButtonClickListener(onButtonClickListener);
        whatsapp.setOnButtonClickListener(onButtonClickListener);
    }

    private boolean isAppInstalled(String packageName) {
        try {
            getContext().getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(LinkChannelSelector linkChannelSelector, List<LinkChannel> channels);
    }

    public interface OnClickLinkChannelListener {
        void onClickLinkChannel(LinkChannelSelector linkChannelSelector,
                                LinkChannelSelectorComponent linkChannelSelectorComponent, LinkChannel linkChannel);
    }
}
