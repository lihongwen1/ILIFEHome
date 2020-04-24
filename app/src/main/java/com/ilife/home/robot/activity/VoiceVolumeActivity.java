package com.ilife.home.robot.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class VoiceVolumeActivity extends BackBaseActivity {
    private String physicalId;
    @BindView(R.id.bt_save_volume)
    TextView tv_save_volume;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.sk_voice_volume)
    SeekBar sk_voice_volume;
    @BindView(R.id.tv_voice_volume)
    TextView tv_voice_volume;
    @BindView(R.id.iv_volume_switch)
    ImageView iv_volume_switch;

    @Override
    public int getLayoutId() {
        return R.layout.activity_voice_volume;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.setting_set_volumn);
        sk_voice_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_voice_volume.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        int volume = IlifeAli.getInstance().getWorkingDevice().getDeviceInfo().getVoiceVolume();
        boolean isOpen = IlifeAli.getInstance().getWorkingDevice().getDeviceInfo().isVoiceOpen();
        sk_voice_volume.setProgress(volume);
        tv_voice_volume.setText(volume + "%");
        iv_volume_switch.setSelected(isOpen);
        sk_voice_volume.setEnabled(isOpen);
        tv_voice_volume.setEnabled(isOpen);

    }

    @OnClick({R.id.bt_save_volume, R.id.iv_volume_switch})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_save_volume:
                showLoadingDialog();
                String jsonStr = "{\"BeepVolume\":1}";
                JSONObject jo = JSONObject.parseObject(jsonStr);
                jo.put(EnvConfigure.KEY_BeepVolume, sk_voice_volume.getProgress());

                String beepJson = "{\"BeepNoDisturb\":{\"Switch\":1,\"Time\":0}}";
                JSONObject bj = JSONObject.parseObject(beepJson);
                bj.getJSONObject(EnvConfigure.KEY_BEEP_NO_DISTURB).put(EnvConfigure.KEY_SWITCH, iv_volume_switch.isSelected()?0:1);
                IlifeAli.getInstance().setProperties(bj, value -> IlifeAli.getInstance().setProperties(jo, aBoolean -> {
                    if (aBoolean) {
                        ToastUtils.showToast(UiUtil.getString(R.string.setting_success));
                        removeActivity();
                    }
                }));

                break;
            case R.id.iv_volume_switch:
                boolean isOpen = !iv_volume_switch.isSelected();
                iv_volume_switch.setSelected(isOpen);
                sk_voice_volume.setEnabled(isOpen);
                tv_voice_volume.setEnabled(isOpen);
                break;


        }
    }


}
