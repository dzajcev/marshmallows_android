package com.dzaitsev.marshmallow.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dzaitsev.marshmallow.R;

public class LinkChannelSelector extends ConstraintLayout {
    private ImageView background;
    private ImageView checkbox;
    private boolean isChecked;

    private OnCheckedChangeListener onCheckedChangeListener;

    public LinkChannelSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initControl(context);
    }

    public LinkChannelSelector(Context context) {
        super(context);
        initControl(context);
    }

    @Override
    public void onViewAdded(View view) {
        super.onViewAdded(view);
    }

    private void initControl(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.link_channel_selector, this);
        background = findViewById(R.id.background);
        checkbox = findViewById(R.id.checkbox);
        checkbox.setVisibility(GONE);
        this.setOnClickListener(v -> {
            isChecked = !isChecked;
            fireChecked();
        });
    }

    private void fireChecked() {
        if (isChecked) {
            checkbox.setVisibility(View.VISIBLE);
        } else {
            checkbox.setVisibility(View.GONE);
        }
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(this, isChecked);
        }
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
        fireChecked();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setBackground(Drawable drawable) {
        post(() -> {
            checkbox.getLayoutParams().width = background.getWidth() / 2;
            checkbox.getLayoutParams().height = background.getHeight() / 2;
            checkbox.requestLayout();
        });

        background.setBackground(drawable);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(LinkChannelSelector linkChannelSelector, boolean isChecked);
    }

}
