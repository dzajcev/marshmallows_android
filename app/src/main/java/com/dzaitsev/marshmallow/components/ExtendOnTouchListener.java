package com.dzaitsev.marshmallow.components;

import android.annotation.SuppressLint;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.dzaitsev.marshmallow.R;

import java.util.function.Supplier;

public class ExtendOnTouchListener implements View.OnTouchListener {
    private Supplier<Boolean> perfomClick;

    private ExecuteOnTouch executeOnTouch;

    public ExtendOnTouchListener(Supplier<Boolean> perfomCLick) {
        this.perfomClick = perfomCLick;
    }

    public ExtendOnTouchListener(ExecuteOnTouch executeOnTouch) {
        this.executeOnTouch = executeOnTouch;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                v.getBackground().setColorFilter(new BlendModeColorFilter(ContextCompat.getColor(v.getContext(), R.color.row_2), BlendMode.COLOR));
                v.invalidate();
                if (perfomClick != null) {
                    return perfomClick.get();
                } else {
                    executeOnTouch.execute();
                    return true;
                }
            }
            case MotionEvent.ACTION_UP -> {
                v.getBackground().clearColorFilter();
                v.invalidate();
            }
        }
        return false;
    }

    public interface ExecuteOnTouch {
        void execute();
    }
}
