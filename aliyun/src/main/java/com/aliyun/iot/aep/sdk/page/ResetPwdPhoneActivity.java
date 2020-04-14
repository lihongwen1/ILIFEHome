package com.aliyun.iot.aep.sdk.page;

import android.os.Bundle;

import com.alibaba.sdk.android.openaccount.ui.OpenAccountUIConfigs;
import com.alibaba.sdk.android.openaccount.ui.ui.ResetPasswordActivity;

public class ResetPwdPhoneActivity extends ResetPasswordActivity {
    @Override
    protected String getLayoutName() {
        return "activity_reset_pwd_phone";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenAccountUIConfigs.MobileResetPasswordLoginFlow.resetPasswordPasswordActivityClazz = ResetPWDFillActivity.class;
    }
}
