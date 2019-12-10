package com.aliyun.iot.aep.sdk.page;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

/**
 * 可以自由翻转的radiobutton
 */
public class ToggleRadioButton extends RadioButton {
    public ToggleRadioButton(Context context) {
        super(context);
    }

    public ToggleRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ToggleRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return RadioButton.class.getName();
    }
}
