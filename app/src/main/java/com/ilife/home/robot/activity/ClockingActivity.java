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
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.RobotConfigBean;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.AlertDialogUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.TimePickerUIUtil;
import com.ilife.home.robot.R;
import com.ilife.home.robot.adapter.ClockAdapter;
import com.ilife.home.robot.entity.NewClockInfo;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.SpaceItemDecoration;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.util.ArrayList;
import java.util.List;

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
    ArrayList<NewClockInfo> clockInfos = new ArrayList<>();
    int hour, minute, open;
    String last;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private int selectPosition;
    private UniversalDialog workTimeDialog;

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
        byte hour = (byte) clockInfos.get(position).getHour();
        byte minute = (byte) clockInfos.get(position).getMinute();
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
                    RobotConfigBean.RobotBean rBean = MyApplication.getInstance().readRobotConfig().getRobotBeanByPk(IlifeAli.getInstance().getWorkingDevice().getProductKey());
                    if (rBean.isScheduleInDark()) {//V3x，X787没有黑暗环境限制
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

    /**
     * position 0-6《-----》schedule1-schedule7
     *
     * @param position
     * @param open
     */
    private void setSchedule(int position, int open) {
        RobotConfigBean.RobotBean rBean = MyApplication.getInstance().readRobotConfig().getRobotBeanByPk(IlifeAli.getInstance().getWorkingDevice().getProductKey());
        IlifeAli.getInstance().setSchedule(rBean.isNewScheduleVersion(), position + 1, open, hour, minute, new OnAliResponse<ScheduleBean>() {
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
        IlifeAli.getInstance().getScheduleInfo(new OnAliResponse<List<ScheduleBean>>() {
            @Override
            public void onSuccess(List<ScheduleBean> scheduleBeans) {
                clockInfos.clear();
                for (ScheduleBean bean : scheduleBeans) {
                    clockInfos.add(new NewClockInfo(bean));
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
