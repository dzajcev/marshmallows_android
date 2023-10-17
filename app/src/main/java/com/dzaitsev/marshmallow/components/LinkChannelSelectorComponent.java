package com.dzaitsev.marshmallow.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.LinkChannel;

public class LinkChannelSelectorComponent extends ConstraintLayout {
    private ImageView background;
    private ImageView checkbox;
    private boolean isChecked;

    private final LinkChannel linkChannel;

    private OnCheckedChangeListener onCheckedChangeListener;

    private OnButtonClickListener onButtonClickListener;

    public enum Mode {
        CHECKBOX(0), BUTTON(1), NONE(-1),
        ;

        private final int idx;

        Mode(int idx) {
            this.idx = idx;
        }

        public int getIdx() {
            return idx;
        }
    }

    private Mode mode = Mode.NONE;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public LinkChannelSelectorComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinkChannelSelectorComponent, 0, 0)) {
            linkChannel = LinkChannel.values()[a.getInt(R.styleable.LinkChannelSelectorComponent_linkChannelType, 0)];
        }
        initControl(context);
    }


    public LinkChannel getLinkChannel() {
        return linkChannel;
    }


    @Override
    public void onViewAdded(View view) {
        super.onViewAdded(view);
    }

    private void initControl(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.link_channel_selector_component, this);
        background = findViewById(R.id.background);
        this.setOnTouchListener(new ExtendOnTouchListener(this::performClick));
    }

    @Override
    public boolean performClick() {
        super.performClick();
        isChecked = !isChecked;
        fireChecked();
        return true;
    }

    private void fireChecked() {
        if (mode == Mode.CHECKBOX) {
            if (isChecked) {
                checkbox.setVisibility(View.VISIBLE);
            } else {
                checkbox.setVisibility(View.GONE);
            }
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(this, isChecked);
            }
        } else {
            if (onButtonClickListener != null) {
                onButtonClickListener.onClick(this, linkChannel);
            }
        }
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
        fireChecked();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.CHECKBOX) {
            checkbox = findViewById(R.id.checkbox);
            checkbox.setVisibility(GONE);
        }
    }

    public void setDimensions(int width, int height) {
        post(() -> {
            background.getLayoutParams().width = width;
            background.getLayoutParams().height = height;
            if (mode == Mode.CHECKBOX) {
                checkbox.getLayoutParams().width = width / 2;
                checkbox.getLayoutParams().height = height / 2;
                checkbox.requestLayout();
            }
        });
    }

    public void setBackground(Drawable drawable) {
        post(() -> {
            if (mode == Mode.CHECKBOX) {
                checkbox.getLayoutParams().width = background.getWidth() / 2;
                checkbox.getLayoutParams().height = background.getHeight() / 2;
                checkbox.requestLayout();
            }
            background.setBackground(drawable);
            background.requestLayout();
        });
    }

    public Drawable getBackground() {
        return background.getBackground();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public void setOnButtonClickListener(OnButtonClickListener onClickListener) {
        this.onButtonClickListener = onClickListener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(LinkChannelSelectorComponent linkChannelSelectorComponent, boolean isChecked);
    }

    public interface OnButtonClickListener {
        void onClick(LinkChannelSelectorComponent linkChannelSelectorComponent, LinkChannel linkChannel);
    }

}
