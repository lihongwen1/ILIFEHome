package com.ilife.home.robot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ilife.home.robot.app.MyApplication;

public class AvantGardTextView extends TextView {

    public AvantGardTextView(Context context) {
        super(context);
        setTypeFace();
    }

    public AvantGardTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeFace();
    }

    public AvantGardTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeFace();
    }

    public void setTypeFace() {
        if (!isInEditMode()) {
            setTypeface(MyApplication.getInstance().avantGard);
        }
    }
}
