package com.aliyun.iot.aep.sdk.page;

import android.os.Bundle;

import com.alibaba.sdk.android.openaccount.ui.OpenAccountUIConfigs;
import com.alibaba.sdk.android.openaccount.ui.ui.EmailResetPasswordActivity;

public class ResetPwdEmailActivity extends EmailResetPasswordActivity {
    @Override
    protected String getLayoutName() {
        return "activity_reset_pwd_email";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenAccountUIConfigs.EmailResetPasswordLoginFlow.resetPasswordActivityClazz=EmailResetFillActivity.class;
    }
}
