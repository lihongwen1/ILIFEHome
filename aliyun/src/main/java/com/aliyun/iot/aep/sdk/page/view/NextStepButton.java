package com.aliyun.iot.aep.sdk.page.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;

import com.alibaba.sdk.android.openaccount.ui.util.AttributeUtils;
import com.alibaba.sdk.android.openaccount.ui.widget.LinearLayoutTemplate;

public class NextStepButton extends LinearLayoutTemplate {
    protected Button button = (Button)this.findViewById("next");

    public NextStepButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typeArray = context.obtainStyledAttributes(attrs, new int[]{16843087});
        String text = typeArray.getString(0);
        if (!TextUtils.isEmpty(text)) {
            this.button.setText(text);
        }

        typeArray.recycle();
        this.useCustomAttrs(context, attrs);
    }

    protected String getLayoutName() {
        return "layout_next_button";
    }

    protected void doUseCustomAttrs(Context context, TypedArray typedArray) {
        if (!this.isInEditMode()) {
            this.button.setBackgroundResource(AttributeUtils.getResourceId(context, typedArray, "ali_sdk_openaccount_attrs_next_step_bg"));
        }
    }

    public void setText(String text) {
        this.button.setText(text);
    }
}
