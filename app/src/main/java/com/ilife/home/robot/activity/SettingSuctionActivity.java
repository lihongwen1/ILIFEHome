package com.ilife.home.robot.activity;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSeekBar;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingSuctionActivity extends BackBaseActivity {
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.sk_robot_set)
    AppCompatSeekBar sk_robot_set;
    @BindView(R.id.tv_robot_set)
    TextView tv_robot_set;

    @BindView(R.id.tv_subtitle)
    TextView tv_subtitle;

    @Override
    public int getLayoutId() {
        return R.layout.activity_setting_robot;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.setting_set_max);
        tv_subtitle.setText(R.string.setting_max_number);
        int progress = IlifeAli.getInstance().getWorkingDevice().getDeviceInfo().getSuctionNumber();
        tv_robot_set.setText(progress + "%");
        sk_robot_set.setProgress(progress);
        sk_robot_set.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_robot_set.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @OnClick(R.id.bt_save_robot_set)
    public void onClick(View view) {
        String jsonStr = "{\"FanPower\":1}";
        JSONObject jo = JSONObject.parseObject(jsonStr);
        jo.put(EnvConfigure.KEY_FanPower, sk_robot_set.getProgress());
        showLoadingDialog();
        IlifeAli.getInstance().setProperties(jo, aBoolean -> {
            hideLoadingDialog();
            if (aBoolean) {
                ToastUtils.showToast(UiUtil.getString(R.string.setting_success));
                removeActivity();
            }
        });
    }

    private boolean canOperateSuction() {
        int curWorkMode = IlifeAli.getInstance().getWorkingDevice().getWork_status();
        if ((curWorkMode == MsgCodeUtils.STATUE_POINT || curWorkMode == MsgCodeUtils.STATUE_RECHARGE)) {
            return false;
        } else {
            return true;
        }
    }
}
