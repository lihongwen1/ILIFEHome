package com.ilife.home.robot.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.ilife.home.robot.R;

public class IlifeToolBar extends Toolbar{
    private TextView mTitleTextView;
    private CharSequence mTitleText;
    private int mTitleTextColor;
    private int mTitleTextAppearance;

    public IlifeToolBar(Context context) {
        super(context);
        this.resolveAttribute(context, null, 0);
    }

    public IlifeToolBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.resolveAttribute(context, attrs,0);
    }

    public IlifeToolBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.resolveAttribute(context, attrs, defStyleAttr);
    }

    private void resolveAttribute(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        context = this.getContext();
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IlifeToolbar, defStyleAttr, 0);
        if (this.mTitleTextColor != 0) {
            this.setTitleTextColor(this.mTitleTextColor);
        }
        a.recycle();
        this.post(() -> {
            if (IlifeToolBar.this.getLayoutParams() instanceof LayoutParams) {
                ((LayoutParams)IlifeToolBar.this.getLayoutParams()).gravity =17;
            }

        });
    }
}
