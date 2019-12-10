package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UserUtils;
import com.ilife.home.robot.utils.Utils;

import butterknife.BindView;
import butterknife.OnClick;

public class RenameActivity extends BackBaseActivity {
    @BindView(R.id.et_rename)
    EditText et_rename;
    @BindView(R.id.tv_top_title)
    TextView title;
    @BindView(R.id.image_back)
    ImageView iv_back;
    @BindView(R.id.image_menu)
    ImageView iv_finish;
    @BindView(R.id.fl_top_menu)
    View fl_top_menu;
    public static final String KEY_RENAME_TYPE = "rename_type";
    private int renameType = 1;

    @Override
    public int getLayoutId() {
        return R.layout.activity_rename;
    }

    @Override
    public void initData() {
        super.initData();
        renameType = getIntent().getIntExtra(KEY_RENAME_TYPE, 1);
    }

    @Override
    public void initView() {
        iv_back.setImageResource(R.drawable.nav_button_cancel);
        iv_finish.setImageResource(R.drawable.nav_button_finish);
        fl_top_menu.setVisibility(View.VISIBLE);
        if (renameType == 1) {
            title.setText(R.string.setting_rename_robot);
            String nickName = IlifeAli.getInstance().getWorkingDevice().getNickName();
            if (nickName == null || nickName.isEmpty()) {
                et_rename.setText(IlifeAli.getInstance().getWorkingDevice().getDeviceName());
            } else {
                et_rename.setText(nickName);
            }
        } else {

            title.setText(R.string.personal_aty_rename);
            et_rename.setText(IlifeAli.getInstance().getUserInfo().userNick);
        }
        UserUtils.setInputFilter(et_rename, Utils.getInputMaxLength());
        et_rename.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @OnClick({R.id.image_back, R.id.image_menu})
    public void Onclick(View view) {
        switch (view.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.image_menu:
                if (renameType == 1) {
                    resetNickname();
                } else {
                    resetUserName();
                }
                break;

        }
    }


    /**
     * 修改设备昵称
     */
    private void resetNickname() {
        String name = et_rename.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtils.showToast(context, getString(R.string.setting_rename_empty));
            return;
        }
        int maxLength;
        if (Utils.isChinaEnvironment()) {
            maxLength = 12;
        } else {
            maxLength = 30;
        }
        if (name.length() > maxLength) {
            ToastUtils.showToast(getResources().getString(R.string.name_max_length, maxLength + ""));
            return;
        }
        IlifeAli.getInstance().reNameDevice(name, isSuccess -> {
            if (isSuccess) {
                ToastUtils.showToast(context, context.getString(R.string.bind_aty_reName_suc));
                IlifeAli.getInstance().getWorkingDevice().setNickName(name);
                finish();
            } else {
                ToastUtils.showToast(context, context.getString(R.string.bind_aty_reName_fail));
            }

        });
    }


    /**
     * 修改用户昵称
     */
    private void resetUserName() {
        String name = et_rename.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtils.showToast(context, getString(R.string.setting_aty_devName_null));
            return;
        }

        if (name.length() > Utils.getInputMaxLength()) {
            ToastUtils.showToast(getResources().getString(R.string.name_max_length, Utils.getInputMaxLength() + ""));
            return;
        }
        IlifeAli.getInstance().resetNickName(name, isSuccess -> runOnUiThread(() -> {
            if (isSuccess) {
                ToastUtils.showToast(context, getString(R.string.personal_aty_reset_suc));
                finish();
            } else {
                ToastUtils.showToast(context, getString(R.string.personal_aty_reset_fail));
            }
        }));
    }
}

