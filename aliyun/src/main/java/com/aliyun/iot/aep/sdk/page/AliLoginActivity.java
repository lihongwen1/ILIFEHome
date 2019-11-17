package com.aliyun.iot.aep.sdk.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.openaccount.OauthService;
import com.alibaba.sdk.android.openaccount.OpenAccountSDK;
import com.alibaba.sdk.android.openaccount.callback.LoginCallback;
import com.alibaba.sdk.android.openaccount.model.OpenAccountSession;
import com.alibaba.sdk.android.openaccount.ui.LayoutMapping;
import com.alibaba.sdk.android.openaccount.ui.OpenAccountUIConfigs;
import com.alibaba.sdk.android.openaccount.ui.OpenAccountUIService;
import com.alibaba.sdk.android.openaccount.ui.callback.EmailRegisterCallback;
import com.alibaba.sdk.android.openaccount.ui.callback.EmailResetPasswordCallback;
import com.alibaba.sdk.android.openaccount.ui.widget.TitleBar;
import com.alibaba.sdk.android.openaccount.util.ResourceUtils;
import com.aliyun.iot.R;
import com.aliyun.iot.aep.sdk.dialog.RegisterSelectorDialogFragment;
import com.aliyun.iot.aep.sdk.dialog.ResetSelectorDialogFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by feijie.xfj on 2018/6/26.
 */

public class AliLoginActivity extends com.alibaba.sdk.android.openaccount.ui.ui.LoginActivity {
    private RegisterSelectorDialogFragment registerSelectorDialogFragment;
    private ResetSelectorDialogFragment resetSelectorDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //显示登录页和手机忘记密码页的选择国家区号
        super.onCreate(savedInstanceState);
        OpenAccountUIConfigs.AccountPasswordLoginFlow.supportForeignMobileNumbers = true;
        OpenAccountUIConfigs.MobileResetPasswordLoginFlow.supportForeignMobileNumbers = true;
        TRANSPARENT();
        registerSelectorDialogFragment = new RegisterSelectorDialogFragment();
        registerSelectorDialogFragment.setOnClickListener(registerListenr);
        resetSelectorDialogFragment = new ResetSelectorDialogFragment();
        resetSelectorDialogFragment.setOnClickListener(resetListenr);
        this.resetPasswordTV = (TextView) this.findViewById(ResourceUtils.getRId(this, "reset_password"));
        if (this.resetPasswordTV != null) {
            this.resetPasswordTV.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    resetSelectorDialogFragment.showAllowingStateLoss(getSupportFragmentManager(), "");
                }
            });
        }
        this.registerTV = (TextView) this.findViewById(ResourceUtils.getRId(this, "register"));
        if (this.registerTV != null) {
            this.registerTV.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    registerSelectorDialogFragment.showAllowingStateLoss(getSupportFragmentManager(), "");
                }
            });
        }
        if (getSupportActionBar() != null) {
            //隐藏掉navigation bar button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }


    private View.OnClickListener registerListenr = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_register_phone) {//手机注册
                OpenAccountUIService openAccountUIService = OpenAccountSDK.getService(OpenAccountUIService.class);
//                openAccountUIService.showRegister(AliLoginActivity.this, getRegisterLoginCallback());
                openAccountUIService.showRegister(AliLoginActivity.this, AliRegisterActivity.class, getLoginCallback());
                registerSelectorDialogFragment.dismissAllowingStateLoss();
            } else if (v.getId() == R.id.btn_register_email) {//邮箱注册
                OpenAccountUIService openAccountUIService = OpenAccountSDK.getService(OpenAccountUIService.class);
//                openAccountUIService.showEmailRegister(AliLoginActivity.this, getEmailRegisterCallback());
                openAccountUIService.showEmailRegister(AliLoginActivity.this, AliRegisterEmaiActivity.class, getEmailRegisterCallback());
                registerSelectorDialogFragment.dismissAllowingStateLoss();
            }
        }
    };


    private View.OnClickListener resetListenr = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_register_phone) {//手机找回
                forgetPhonePassword(v);
                resetSelectorDialogFragment.dismissAllowingStateLoss();
            } else if (v.getId() == R.id.btn_register_email) {//邮箱找回
                forgetMailPassword(v);
                resetSelectorDialogFragment.dismissAllowingStateLoss();
            }
        }
    };

    public void forgetPhonePassword(View view) {
        Map<String, String> ext = new HashMap();
        if (!TextUtils.isEmpty(this.loginIdEdit.getInputBoxWithClear().getMobileLocationCode())) {
            ext.put("LocationCode", this.loginIdEdit.getInputBoxWithClear().getMobileLocationCode());
        }

        ext.put("mobile", this.loginIdEdit.getEditText().getText().toString());
        OpenAccountUIService openAccountUIService = (OpenAccountUIService) OpenAccountSDK.getService(OpenAccountUIService.class);
        openAccountUIService.showResetPassword(this, ext, ResetPwdPhoneActivity.class, getResetPasswordLoginCallback());
    }

    public void forgetMailPassword(View view) {
        OpenAccountUIService openAccountUIService = (OpenAccountUIService) OpenAccountSDK.getService(OpenAccountUIService.class);
//        openAccountUIService.showEmailResetPassword(this, this.getEmailResetPasswordCallback());
        openAccountUIService.showEmailResetPassword(this, ResetPwdEmailActivity.class, getEmailResetPasswordCallback());
    }

    private EmailResetPasswordCallback getEmailResetPasswordCallback() {
        return new EmailResetPasswordCallback() {

            @Override
            public void onSuccess(OpenAccountSession session) {
                LoginCallback callback = getLoginCallback();
                if (callback != null) {
                    callback.onSuccess(session);

                }
                finishWithoutCallback();
            }

            @Override
            public void onFailure(int code, String message) {
                LoginCallback callback = getLoginCallback();
                if (callback != null) {
                    callback.onFailure(code, message);
                }
            }

            @Override
            public void onEmailSent(String email) {
                Toast.makeText(getApplicationContext(), email + " 已经发送了", Toast.LENGTH_LONG).show();
            }

        };
    }

    private EmailRegisterCallback getEmailRegisterCallback() {
        return new EmailRegisterCallback() {

            @Override
            public void onSuccess(OpenAccountSession session) {
                LoginCallback callback = getLoginCallback();
                if (callback != null) {
                    callback.onSuccess(session);
                }
                finishWithoutCallback();
            }

            @Override
            public void onFailure(int code, String message) {
                LoginCallback callback = getLoginCallback();
                if (callback != null) {
                    callback.onFailure(code, message);
                }
            }

            @Override
            public void onEmailSent(String email) {
                Toast.makeText(getApplicationContext(), email + " 已经发送了", Toast.LENGTH_LONG).show();
            }

        };
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OauthService service = OpenAccountSDK.getService(OauthService.class);
        if (service != null) {
            service.authorizeCallback(requestCode, resultCode, data);
        }
    }


    protected final void TRANSPARENT() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }


    @Override
    protected String getLayoutName() {
        return "activity_ali_login";
    }
}
