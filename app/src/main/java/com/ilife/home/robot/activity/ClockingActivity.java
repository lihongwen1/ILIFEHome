package com.ilife.home.robot.activity;

import android.app.Dialog;
import android.content.Intent;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.ScheduleBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
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
import butterknife.OnClick;


/**
 * Created by chenjiaping on 2017/7/25.
 */

public class ClockingActivity extends BackBaseActivity {
    final String TAG = ClockingActivity.class.getSimpleName();
    RecyclerView recyclerView;
    ClockAdapter adapter;
    LayoutInflater inflater;
    SmartRefreshLayout refreshLayout;
    ArrayList<NewClockInfo> clockInfos = new ArrayList<>();
    @BindView(R.id.tv_top_title)
    TextView tv_title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            Intent intent = new Intent(ClockingActivity.this, ClockEditActivity.class);
            intent.putExtra(ClockEditActivity.KEY_SCHEDULE_INFO, clockInfos.get(position));
            startActivity(intent);
        });
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.image_status) {//开关按钮点击事件
                NewClockInfo clockInfo = clockInfos.get(position);
                clockInfo.setOpen(clockInfo.getOpen() == 1 ? 0 : 1);
                setSchedule(clockInfo);
            }

        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }


    @OnClick(R.id.ivb_add_clock)
    public void onClick(View view) {
        Intent intent = new Intent(ClockingActivity.this, ClockEditActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
       getClockInfo();
    }

    /**
     * position 0-6《-----》schedule1-schedule7
     * @param clockInfo
     */
    private void setSchedule(NewClockInfo clockInfo) {
        String scheduleJson = JSON.toJSONString(clockInfo.toScheduleBean());
        JSONObject jsoSchedule = JSONObject.parseObject(scheduleJson);
        String json_str = "{\"Schedule\":\" \"}";
        String schedule_ = json_str.replaceFirst(EnvConfigure.KEY_SCHEDULE, clockInfo.getScheduleKey());
        JSONObject jso = JSONObject.parseObject(schedule_);
        jso.put(clockInfo.getScheduleKey(), jsoSchedule);
        IlifeAli.getInstance().setProperties(jso, aBoolean -> {
            if (aBoolean) {
                adapter.notifyDataSetChanged();
            } else {
                clockInfo.setOpen(clockInfo.getOpen() == 1 ? 0 : 1);
            }
        });
    }

    public void getClockInfo() {
        IlifeAli.getInstance().getScheduleInfo(new OnAliResponse<List<ScheduleBean>>() {
            @Override
            public void onSuccess(List<ScheduleBean> scheduleBeans) {
                clockInfos.clear();
                int key_index = 1;
                for (ScheduleBean bean : scheduleBeans) {
                    NewClockInfo clockInfo = new NewClockInfo(bean);
                    clockInfo.setMode(6);//预约模式默认规划
                    clockInfo.setScheduleKey(EnvConfigure.KEY_SCHEDULE + key_index);
                    key_index++;
                    clockInfos.add(clockInfo);
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
