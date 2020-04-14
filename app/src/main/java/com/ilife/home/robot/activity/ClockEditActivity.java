package com.ilife.home.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot._interface.OnDialogClick;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.entity.NewClockInfo;
import com.ilife.home.robot.fragment.BottomSheetSelectDialog;
import com.ilife.home.robot.fragment.TimeSelectDialog;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 定时预约编辑界面
 */
public class ClockEditActivity extends BackBaseActivity {
    public static final String KEY_SCHEDULE_INFO = "schedule_info";

    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.tv_schedule_time)
    TextView tv_schedule_time;
    @BindView(R.id.tv_schedule_loop)
    TextView tv_schedule_loop;
    @BindView(R.id.tv_schedule_mode)
    TextView tv_schedule_mode;
    @BindView(R.id.tv_schedule_times)
    TextView tv_schedule_times;
    @BindView(R.id.tv_schedule_area)
    TextView tv_schedule_area;
    private TimeSelectDialog mTimeDialog;
    private BottomSheetSelectDialog textSelectDialog;
    private NewClockInfo mClockInfo;
    private int mTextSelectorType;

    @Override
    public int getLayoutId() {
        return R.layout.activity_clock_edit;
    }

    @Override
    public void initData() {
        super.initData();
        mClockInfo = getIntent().getParcelableExtra(KEY_SCHEDULE_INFO);
        if (mClockInfo == null) {
            mClockInfo = new NewClockInfo();
        }
        LiveEventBus.get(ScheduleAreaActivity.LIVE_BUS_SCHEDULE_AREA_TYPE, Integer.class)
                .observe(this, type -> {
                    mClockInfo.setType(type);
                    setScheduleArea(mClockInfo.getType());
                });
        LiveEventBus.get(ScheduleAreaActivity.LIVE_BUS_SCHEDULE_AREA_DATA, String.class)
                .observe(this, s -> {
                    /**
                     * 选房清扫数据和划区清扫数据只能二选一
                     */
                    if (mClockInfo.getType()==0){
                        mClockInfo.setArea("AAAAAAAAAAAAAAAAAAAAAA==");
                        mClockInfo.setRoom(0);
                    }
                    if (mClockInfo.getType() == 1) {//分区
                        mClockInfo.setArea(s);
                        mClockInfo.setRoom(0);
                    }
                    if (mClockInfo.getType() == 2) {//分房
                        mClockInfo.setRoom(Integer.valueOf(s));
                        mClockInfo.setArea("AAAAAAAAAAAAAAAAAAAAAA==");
                    }
                });
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.clock_aty_appoint);
        setScheduleTime(mClockInfo.getHour(), mClockInfo.getMinute());
        setScheduleLoop(mClockInfo.getWeek());
        setScheduleMode(mClockInfo.getMode());
        setScheduleTimes(mClockInfo.getTimes());
        setScheduleArea(mClockInfo.getType());
    }

    private void setScheduleTime(int i_hour, int i_minute) {
        String hour = i_hour < 10 ? "0" + i_hour : "" + i_hour;
        String minute = i_minute < 10 ? "0" + i_minute : "" + i_minute;
        tv_schedule_time.setText(hour + ":" + minute);
    }

    private void setScheduleLoop(int week) {
        tv_schedule_loop.setText(DataUtils.getScheduleWeek(week));
    }

    private void setScheduleMode(int mode) {
        if (mode == 0) {
            mode = MsgCodeUtils.STATUE_PLANNING;
        }
        tv_schedule_mode.setText(DeviceUtils.getStatusStr(this, mode, 0));
    }

    private void setScheduleTimes(int times) {
        tv_schedule_times.setText(DataUtils.getScheduleTimes(times));
    }

    private void setScheduleArea(int type) {
        String area = "";
        switch (type) {
            case 0:
                area = UiUtil.getString(R.string.clock_area_default);
                break;
            case 1:
                area = UiUtil.getString(R.string.clock_area_clean_area);
                break;
            case 2:
                area =UiUtil.getString(R.string.clock_area_choose_room);
                break;
        }
        tv_schedule_area.setText(area);
    }

    @OnClick({R.id.ll_schedule_time, R.id.ll_schedule_area, R.id.ll_schedule_loop, R.id.ll_schedule_mode, R.id.ll_schedule_times, R.id.btn_save_schedule, R.id.btn_cancel_schedule})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_schedule_time://预约时间
                onScheduleTimeClick();
                break;
            case R.id.ll_schedule_loop://预约重复时间
                mTextSelectorType = 1;
                showTextSelectorDialog(true, UiUtil.getString(R.string.clock_repeat), R.array.text_week, tv_schedule_loop.getText().toString().trim());
                break;
            case R.id.ll_schedule_mode://预约清扫模式
                mTextSelectorType = 2;
                showTextSelectorDialog(false, "", R.array.text_mode, tv_schedule_mode.getText().toString().trim());
                break;
            case R.id.ll_schedule_times://预约清扫次数
                mTextSelectorType = 3;
                showTextSelectorDialog(false, UiUtil.getString(R.string.clock_clean_times), R.array.text_clean_times, tv_schedule_times.getText().toString().trim());
                break;
            case R.id.ll_schedule_area://预约清扫区域
                Intent intent = new Intent(ClockEditActivity.this, ScheduleAreaActivity.class);
                intent.putExtra(KEY_SCHEDULE_INFO, mClockInfo);
                startActivity(intent);
                break;
            case R.id.btn_cancel_schedule:
                removeActivity();
                break;
            case R.id.btn_save_schedule:
                mClockInfo.setOpen(1);
                String scheduleJson = JSON.toJSONString(mClockInfo.toScheduleBean());
                JSONObject jsoSchedule = JSONObject.parseObject(scheduleJson);
                String json_str = "{\"Schedule\":\" \"}";
                String schedule_ = json_str.replaceFirst(EnvConfigure.KEY_SCHEDULE, mClockInfo.getScheduleKey());
                JSONObject jso = JSONObject.parseObject(schedule_);
                jso.put(mClockInfo.getScheduleKey(), jsoSchedule);
                IlifeAli.getInstance().setProperties(jso, aBoolean -> {
                    if (aBoolean) {
                        ToastUtils.showToast("设置预约数据成功");
                        removeActivity();

                    }
                });
                //保存预约数据
                break;
        }
    }

    /**
     * 设置预约时间
     */
    private void onScheduleTimeClick() {
        if (mTimeDialog == null) {
            mTimeDialog = new TimeSelectDialog();
            mTimeDialog.setOnDialogClick(new OnDialogClick<int[]>() {
                @Override
                public void onCancelClick() {
                }

                @Override
                public void onConfirmClick(int[] time) {
                    mClockInfo.setHour(time[0]);
                    mClockInfo.setMinute(time[1]);
                    setScheduleTime(mClockInfo.getHour(), mClockInfo.getMinute());
                }
            });

        }
        if (!mTimeDialog.isAdded()) {
            Bundle bundle = new Bundle();
            bundle.putIntArray(TimeSelectDialog.KEY_TIME_HOUR, new int[]{mClockInfo.getHour(), mClockInfo.getMinute()});
            mTimeDialog.setArguments(bundle);
            mTimeDialog.show(getSupportFragmentManager(), "time_selector");
        }
    }

    private void showTextSelectorDialog(boolean supportCheck, String title, int arrayId, String currentValue) {
        if (textSelectDialog == null) {
            BottomSheetSelectDialog.Builder builder = new BottomSheetSelectDialog.Builder();
            builder.setCancelOutSide(false).setOnTextSelect((positions, text) -> {
                switch (mTextSelectorType) {
                    case 1://loop
                        mClockInfo.setWeek(getScheduleWeek(positions));
                        setScheduleLoop(mClockInfo.getWeek());
                        break;
                    case 2://mode
                        mClockInfo.setMode(positions[0] == 0 ? MsgCodeUtils.STATUE_PLANNING : MsgCodeUtils.STATUE_RANDOM);
                        setScheduleMode(mClockInfo.getMode());
                        break;
                    case 3://times
                        mClockInfo.setTimes(positions[0] + 1);
                        setScheduleTimes(mClockInfo.getTimes());
                        break;
                }
            });
            textSelectDialog = new BottomSheetSelectDialog(builder);
        }
        if (!textSelectDialog.isAdded()) {
            Bundle bundle = new Bundle();
            bundle.putStringArray(BottomSheetSelectDialog.KEY_TEXT_VALUE, UiUtil.getStringArray(arrayId));
            bundle.putString(BottomSheetSelectDialog.KEY_CURRENT_VALUE, currentValue);
            bundle.putString(BottomSheetSelectDialog.KEY_TITLE, title);
            bundle.putBoolean(BottomSheetSelectDialog.KEY_SUPPORT_CHECK, supportCheck);
            textSelectDialog.setArguments(bundle);
            textSelectDialog.show(getSupportFragmentManager(), "time_selector");
        }
    }

    private int getScheduleWeek(int[] positions) {
        int week = 0;
        for (int position : positions) {
            position = position - 1;
            if (position == -1) {
                position = 6;
            }
            week = DataUtils.setBitTo1(week, position);
        }
        return week;
    }
}
