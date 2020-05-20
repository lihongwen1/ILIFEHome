package com.ilife.home.robot.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;

import butterknife.BindView;
import butterknife.OnClick;

public class VolumeSettingActivity extends BackBaseActivity {
    @BindView(R.id.tv_volume_number)
    TextView tv_volume_number;
    @BindView(R.id.tv_no_disturb_state)
    TextView tv_no_disturb_state;
    @BindView(R.id.tv_volume_language)
    TextView tv_volume_language;
    @BindView(R.id.tv_top_title)
    TextView tv_top_title;

    @Override
    public int getLayoutId() {
        return R.layout.activity_volume_setting;
    }

    @Override
    public void initView() {
        tv_top_title.setText(R.string.setting_voice_setting);
    }

    @Override
    public void initData() {
        super.initData();
        PropertyBean propertyBean=IlifeAli.getInstance().getWorkingDevice().getDeviceInfo();
        tv_volume_number.setText(String.valueOf(propertyBean.getVoiceVolume()));
        tv_volume_language.setText(String.valueOf(DataUtils.getLanguageByCode(propertyBean.getLanguageCode())));
        tv_no_disturb_state.setText(propertyBean.isDisturb() ? R.string.open : R.string.close);
        LiveEventBus.get(EnvConfigure.KEY_BeepVolume, Integer.class).observe(this, value -> {
            MyLogger.d("LiveBus", "收到Live Bus 信息");
            tv_volume_number.setText(String.valueOf(value));
        });
        LiveEventBus.get(EnvConfigure.KEY_BeepType, Integer.class).observe(this, value -> {
            MyLogger.d("LiveBus", "收到Live Bus 信息");
            tv_volume_language.setText(String.valueOf(DataUtils.getLanguageByCode(value)));
        });
        LiveEventBus.get(EnvConfigure.KEY_BEEP_NO_DISTURB, Boolean.class).observe(this, aBoolean -> {
            MyLogger.d("LiveBus", "收到Live Bus 信息");
            tv_no_disturb_state.setText(aBoolean ? R.string.open : R.string.close);
        });
    }

    @OnClick({R.id.ll_volume_adjust, R.id.ll_no_disturb_mode, R.id.ll_volume_language})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_volume_adjust:
                startActivity(new Intent(this, VoiceVolumeActivity.class));
                break;
            case R.id.ll_no_disturb_mode:
                startActivity(new Intent(this, ActivityNoDisturb.class));
                break;
            case R.id.ll_volume_language:
                startActivity(new Intent(this, VoiceLanguageActivity.class));
                break;
        }
    }
}
