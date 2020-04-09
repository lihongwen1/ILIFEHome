package com.ilife.home.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BaseActivity;
import com.ilife.home.robot.bean.RobotConfigBean;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.utils.UserUtils;
import com.ilife.home.robot.utils.Utils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chengjiaping on 2018/9/3.
 */

public class BindSucActivity extends BaseActivity {
    final String TAG = BindSucActivity.class.getSimpleName();
    String name;
    Context context;
    @BindView(R.id.bt_done)
    Button bt_done;
    @BindView(R.id.et_devName)
    EditText et_devName;
    @BindView(R.id.iv_bind_device)
    ImageView iv_bind_device;
    public static final String KEY_BIND_WHITE_DEV_ID = "key_bind_white_dev_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected boolean canGoBack() {
        return false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_bind_suc;
    }

    @Override
    public void initView() {
        context = this;
        et_devName.requestFocus();
    }

    public void initData() {
        RobotConfigBean.RobotBean rBean=MyApplication.getInstance().readRobotConfig().getRobotBeanByPk(IlifeAli.getInstance().getBindingProductKey());
        iv_bind_device.setImageResource(UiUtil.getDrawable(rBean.getFaceImg()));
        et_devName.setText(BuildConfig.BRAND + " "+rBean.getSettingRobot());
        et_devName.setSelection(et_devName.getText().toString().trim().length());
        UserUtils.setInputFilter(et_devName, Utils.getInputMaxLength());
    }

    @OnClick({R.id.bt_done})
    public void onClick(View v) {
        if (v.getId() == R.id.bt_done) {
            name = et_devName.getText().toString().trim();
            if (name.length() > Utils.getInputMaxLength()) {
                ToastUtils.showToast(getResources().getString(R.string.name_max_length, Utils.getInputMaxLength() + ""));
                return;
            }
            if (TextUtils.isEmpty(name)) {
                ToastUtils.showToast(context, getString(R.string.setting_aty_hit));
            } else {
                IlifeAli.getInstance().reNameDevice(name, isSuccess -> {
                    if (isSuccess) {
                        ToastUtils.showToast(context, context.getString(R.string.change_robot_name_suc));
                        Intent i = new Intent(context, MainActivity.class);
                        startActivity(i);
                        removeActivity();
                    } else {//重命名失败,提示连接超时，需重新提交重命名；
                        ToastUtils.showErrorToast(context, 0);
                    }
                });

            }
        }
    }
}
