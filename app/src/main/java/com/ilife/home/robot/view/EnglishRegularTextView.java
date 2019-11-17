package com.ilife.home.robot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ilife.home.robot.app.MyApplication;

public class EnglishRegularTextView extends TextView {

    public EnglishRegularTextView(Context context) {
        super(context);
        setTypeFace();
    }

    public EnglishRegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeFace();
    }

    public EnglishRegularTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeFace();
    }

    public void setTypeFace() {
        if (!isInEditMode()) {
            setTypeface(MyApplication.getInstance().tf_robot_regular);
        }
    }
}
