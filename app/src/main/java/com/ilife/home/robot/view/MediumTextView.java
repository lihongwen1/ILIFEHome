package com.ilife.home.robot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ilife.home.robot.app.MyApplication;

/**
 * Created by chengjiaping on 2018/8/30.
 */

public class MediumTextView extends TextView {
    public MediumTextView(Context context) {
        super(context);
        setTypeFace();
    }

    public MediumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeFace();
    }

    public MediumTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeFace();
    }

    public void setTypeFace() {
        if (!isInEditMode()) {
            setTypeface(MyApplication.getInstance().tf_medium);
        }
    }
}
