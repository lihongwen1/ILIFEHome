package com.ilife.home.robot.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot._interface.OnDialogClick;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.TimeSelectDialog;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.ToastUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class ActivityNoDisturb extends BackBaseActivity {
    @BindView(R.id.iv_no_disturb)
    ImageView iv_no_disturb;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.tv_no_disturb_start_time)
    TextView tv_no_disturb_start_time;
    @BindView(R.id.tv_no_disturb_end_time)
    TextView tv_no_disturb_end_time;
    private TimeSelectDialog mTimeDialog;
    private int timeSelectorType;
    private byte[] noDisturbTime;

    @Override
    public int getLayoutId() {
        return R.layout.activity_no_disturb;
    }

    @Override
    public void initView() {
        if (noDisturbTime != null) {
            tv_no_disturb_start_time.setText(DataUtils.formatTimeNumber(noDisturbTime[0], noDisturbTime[1]));
            tv_no_disturb_end_time.setText(DataUtils.formatTimeNumber(noDisturbTime[2], noDisturbTime[3]));
        }
        iv_no_disturb.setSelected(IlifeAli.getInstance().getWorkingDevice().getDeviceInfo().isDisturb());
        tv_title.setText(R.string.no_disturb_mode);
    }

    @OnClick({R.id.iv_no_disturb, R.id.bt_save_no_disturb, R.id.ll_no_disturb_start_time, R.id.ll_no_disturb_end_time})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_no_disturb:
                iv_no_disturb.setSelected(!iv_no_disturb.isSelected());
                break;
            case R.id.bt_save_no_disturb:
                saveNoDisturb();
                break;
            case R.id.ll_no_disturb_start_time:
                timeSelectorType = 1;
                showTimeSelector();
                break;
            case R.id.ll_no_disturb_end_time:
                showTimeSelector();
                timeSelectorType = 2;
                break;
        }
    }

    private void saveNoDisturb() {
        int[] startTime = DataUtils.parseTimeString(tv_no_disturb_start_time.getText().toString().trim(), ":");
        int[] endTime = DataUtils.parseTimeString(tv_no_disturb_end_time.getText().toString().trim(), ":");
        if (startTime != null) {
            noDisturbTime[0] = (byte) startTime[0];
            noDisturbTime[1] = (byte) startTime[1];
        }
        if (endTime != null) {
            noDisturbTime[2] = (byte) endTime[0];
            noDisturbTime[3] = (byte) endTime[1];
        }
        String beepJson = "{\"BeepNoDisturb\":{\"Switch\":1,\"Time\":0}}";
        JSONObject bj = JSONObject.parseObject(beepJson);
        bj.getJSONObject(EnvConfigure.KEY_BEEP_NO_DISTURB).put(EnvConfigure.KEY_SWITCH, iv_no_disturb.isSelected() ? 1 : 0);
        bj.getJSONObject(EnvConfigure.KEY_BEEP_NO_DISTURB).put("Time", DataUtils.bytesToInt(noDisturbTime));
        showLoadingDialog();
        IlifeAli.getInstance().setProperties(bj, aBoolean -> {
            hideLoadingDialog();
            ToastUtils.showSettingSuccess(aBoolean);
            if (aBoolean) {
                removeActivity();
            }
        });
    }

    private void decodeTime(int time) {
        noDisturbTime = DataUtils.intToBytes4(time);
    }

    @Override
    public void initData() {
        super.initData();
        decodeTime(IlifeAli.getInstance().getWorkingDevice().getDeviceInfo().getDisturbTime());
    }

    /**
     * 设置预约时间
     */
    private void showTimeSelector() {
        if (mTimeDialog == null) {
            mTimeDialog = new TimeSelectDialog();
            mTimeDialog.setOnDialogClick(new OnDialogClick<int[]>() {
                @Override
                public void onCancelClick() {
                }

                @Override
                public void onConfirmClick(int[] time) {
                    if (timeSelectorType == 1) {
                        tv_no_disturb_start_time.setText(DataUtils.formatTimeNumber(time[0], time[1]));
                    } else {
                        tv_no_disturb_end_time.setText(DataUtils.formatTimeNumber(time[0], time[1]));
                    }
                }
            });
        }
        if (!mTimeDialog.isAdded()) {
            Bundle bundle = new Bundle();
            mTimeDialog.setArguments(bundle);
            mTimeDialog.show(getSupportFragmentManager(), "time_selector");
        }
    }
}
