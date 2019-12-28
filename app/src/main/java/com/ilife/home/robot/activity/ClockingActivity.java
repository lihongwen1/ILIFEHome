package com.ilife.home.robot.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.ScheduleBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.AlertDialogUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.TimePickerUIUtil;
import com.ilife.home.robot.R;
import com.ilife.home.robot.adapter.ClockAdapter;
import com.ilife.home.robot.entity.NewClockInfo;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.util.ArrayList;

import butterknife.BindView;


/**
 * Created by chenjiaping on 2017/7/25.
 */

public class ClockingActivity extends BackBaseActivity {
    final String TAG = ClockingActivity.class.getSimpleName();
    final String UNDER_LINE = "_";
    TextView tv_confirm;
    TextView tv_cancel;
    RecyclerView recyclerView;
    ClockAdapter adapter;
    AlertDialog alertDialog;
    LayoutInflater inflater;
    TimePicker timePicker;
    SmartRefreshLayout refreshLayout;
    ArrayList<NewClockInfo> clockInfos;
    int hour, minute, open;
    String[] weeks;
    String last;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private int selectPosition;
    private UniversalDialog workTimeDialog;
    private String robotType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getClockInfo();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_clock;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void initView() {
        context = this;
        tv_title.setText(R.string.clock_aty_appoint);
        inflater = LayoutInflater.from(context);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(refreshLayout -> getClockInfo());
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        adapter = new ClockAdapter(R.layout.layout_clock_item, clockInfos);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            showSetClockDialog(position);
        });
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.image_status) {//开关按钮点击事件
                NewClockInfo clockInfo = clockInfos.get(position);
                hour = clockInfo.getHour();
                minute = clockInfo.getMinute();
                open = clockInfo.getOpen();
                open = open == 1 ? 0 : 1;
                setSchedule(position, open);
            }

        });
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initData() {
        robotType = DeviceUtils.getRobotType(IlifeAli.getInstance().getWorkingDevice().getProductKey());
        clockInfos = new ArrayList<>();
        weeks = getResources().getStringArray(R.array.array_week);
        for (int i = 0; i < 7; i++) {
            NewClockInfo info = new NewClockInfo();
            info.setWeek(weeks[i]);
            info.setHour((byte) 0);
            info.setMinute((byte) 0);
            info.setOpen((byte) 0);
            clockInfos.add(info);
        }
    }

    public void showSetClockDialog(int position) {
        if (alertDialog == null) {
            View contentView = inflater.inflate(R.layout.layout_timepick_dialog, null);
            timePicker = (TimePicker) contentView.findViewById(R.id.timePicker);
            TimePickerUIUtil.set_timepicker_text_colour(timePicker, context);
            tv_confirm = (TextView) contentView.findViewById(R.id.tv_confirm);
            tv_cancel = (TextView) contentView.findViewById(R.id.tv_cancel);
            int width = (int) getResources().getDimension(R.dimen.dp_300);
            int height = (int) getResources().getDimension(R.dimen.dp_300);
            alertDialog = AlertDialogUtils.showDialog(context, contentView, width, height);//
        } else {
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
        tv_cancel.setOnClickListener(new MyListener(position));
        tv_confirm.setOnClickListener(new MyListener(position));
        byte hour = clockInfos.get(position).getHour();
        byte minute = clockInfos.get(position).getMinute();
        last = hour + UNDER_LINE + minute;
        if (DateFormat.is24HourFormat(context)) {
            timePicker.setIs24HourView(true);
        } else {
            timePicker.setIs24HourView(false);
        }
        timePicker.setCurrentHour((int) hour);
        timePicker.setCurrentMinute((int) minute);
    }

    class MyListener implements View.OnClickListener {
        int position;

        public MyListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            AlertDialogUtils.hidden(alertDialog);
            switch (v.getId()) {
                case R.id.tv_cancel:

                    break;
                case R.id.tv_confirm:
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();
                    selectPosition = position;
                    if (robotType.equals(Constants.V3x)||robotType.equals(Constants.X787)) {//V3x，X787没有黑暗环境限制
                        setSchedule(selectPosition, 1);
                    } else if ((hour > 5 && hour < 20) || (hour == 5 && minute > 0)) {//可用时间段(预约夜间时间提醒)
                        setSchedule(selectPosition, 1);
                    } else {//不可用时间
                        if (workTimeDialog == null) {
                            workTimeDialog = new UniversalDialog();
                            workTimeDialog.setTitle(getString(R.string.clock_dialog_title)).setHintTip(getString(R.string.clock_dialog_content))
                                    .setLeftText(getString(R.string.clock_dialog_cancel)).setRightText(getString(R.string.clock_dialog_finish))
                                    .setOnRightButtonClck(() -> setSchedule(selectPosition, 1));
                        }
                        workTimeDialog.show(getSupportFragmentManager(), "work_time");
                    }
                    break;
            }
        }

    }

    private void setSchedule(int position, int open) {
        IlifeAli.getInstance().setSchedule(position, open, hour, minute, new OnAliResponse<ScheduleBean>() {
            @Override
            public void onSuccess(ScheduleBean scheduleBean) {
                NewClockInfo clockInfo = clockInfos.get(position);
                clockInfo.setOpen((byte) scheduleBean.getScheduleEnable());
                clockInfo.setHour((byte) scheduleBean.getScheduleHour());
                clockInfo.setMinute((byte) scheduleBean.getScheduleMinutes());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(int code, String message) {
                MyLogger.d(TAG, "预约设置失败：" + message);
            }
        });
    }

    public void getClockInfo() {
        IlifeAli.getInstance().getScheduleInfo(new OnAliResponse<String>() {
            @Override
            public void onSuccess(String content) {
                MyLogger.d(TAG, "getClockInfo  content:     " + content);
                JSONObject object = JSONObject.parseObject(content);
                for (int i = 0; i < 7; i++) {
                    String week;
                    String key = EnvConfigure.KEY_SCHEDULE + (i + 1);
                    if (!object.containsKey(key)) {
                        MyLogger.d(TAG, "json 数据缺失---：" + key);
                        continue;
                    }
                    int hour = object.getJSONObject(key).getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_SCHEDULE_HOUR);
                    int minute = object.getJSONObject(key).getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_SCHEDULE_MINUTES);
                    int open = object.getJSONObject(key).getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_SCHEDULE_ENABLE);
                    NewClockInfo clockInfo;
                    if (i != 6) {
                        week = weeks[i + 1];
                        clockInfo = clockInfos.get(i + 1);
                    } else {
                        week = weeks[0];
                        clockInfo = clockInfos.get(0);
                    }
                    clockInfo.setWeek(week);
                    clockInfo.setOpen((byte) open);
                    clockInfo.setHour((byte) hour);
                    clockInfo.setMinute((byte) minute);
                }
                adapter.notifyDataSetChanged();
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
            }

            @Override
            public void onFailed(int code, String message) {
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                ToastUtils.showErrorToast(context, code);
            }
        });
    }
}
