package com.aliyun.iot.aep.sdk.page;

import com.alibaba.sdk.android.openaccount.ui.ui.ResetPasswordFillPasswordActivity;

/**
 * 手机号码重置密码
 */
public class ResetPWDFillActivity extends ResetPasswordFillPasswordActivity {
    @Override
    protected String getLayoutName() {
        return "phone_reset_fill_password";
    }
}
