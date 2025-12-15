package com.dzaitsev.marshmallow.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;
import com.dzaitsev.marshmallow.dto.LinkChannel;

import lombok.Getter;
import lombok.Setter;

public class LinkChannelSelectorComponent extends ConstraintLayout {
    private ImageView background;
    private ImageView checkbox;
    @Getter
    private boolean isChecked;

    @Getter
    private final LinkChannel linkChannel;

    @Setter
    private OnCheckedChangeListener onCheckedChangeListener;

    @Setter
    private OnButtonClickListener onButtonClickListener;

    @Getter
    public enum Mode {
        CHECKBOX(0), BUTTON(1), NONE(-1),
        ;

        private final int idx;

        Mode(int idx) {
            this.idx = idx;
        }

    }

    private Mode mode = Mode.NONE;

    public LinkChannelSelectorComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinkChannelSelectorComponent, 0, 0)) {
            linkChannel = LinkChannel.values()[a.getInt(R.styleable.LinkChannelSelectorComponent_linkChannelType, 0)];
        }
        initControl(context);
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
        this.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    v.getBackground().setColorFilter(new BlendModeColorFilter(ContextCompat.getColor(v.getContext(), R.color.row_2), BlendMode.COLOR));
                    v.invalidate();
                    return performClick();
                }
                case MotionEvent.ACTION_UP -> {
                    v.getBackground().clearColorFilter();
                    v.invalidate();
                }
            }
            return false;
        });
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

    public interface OnCheckedChangeListener {
        void onCheckedChanged(LinkChannelSelectorComponent linkChannelSelectorComponent, boolean isChecked);
    }

    public interface OnButtonClickListener {
        void onClick(LinkChannelSelectorComponent linkChannelSelectorComponent, LinkChannel linkChannel);
    }

}
