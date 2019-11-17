package com.ilife.home.robot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.ilife.home.robot.app.MyApplication;

/**
 * Created by chengjiaping on 2018/8/30.
 */

public class LightEditText extends EditText {
    public LightEditText(Context context) {
        super(context);
        setTypeFace();
    }

    public LightEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeFace();
    }

    public LightEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeFace();
    }

    public void setTypeFace() {
        setTypeface(MyApplication.getInstance().tf_light);
    }
}
