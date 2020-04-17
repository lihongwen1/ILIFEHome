package com.ilife.home.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.ScheduleBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.robot.R;
import com.ilife.home.robot.adapter.ClockAdapter;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.view.SlideRecyclerView;
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
    SlideRecyclerView recyclerView;
    ClockAdapter adapter;
    LayoutInflater inflater;
    SmartRefreshLayout refreshLayout;
    List<ScheduleBean> scheduleBeans = new ArrayList<>();
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private WeakHandler weakHandler;
    private int clickPosition;
    private UniversalDialog deleteDialog;

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
        adapter = new ClockAdapter(R.layout.layout_clock_item, scheduleBeans);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            Intent intent = new Intent(ClockingActivity.this, ClockEditActivity.class);
            intent.putExtra(ClockEditActivity.KEY_SCHEDULE_INFO, scheduleBeans.get(position));
            startActivity(intent);
        });
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.image_status) {//开关按钮点击事件
                ScheduleBean bean = scheduleBeans.get(position);
                bean.setEnable(bean.getEnable() == 1 ? 0 : 1);
                showLoadingDialog();
                setSchedule(bean);
            }
            if (view.getId() == R.id.iv_delete_schedule) {//删除预约
                clickPosition = position;
                if (deleteDialog == null) {
                    deleteDialog = new UniversalDialog();
                    deleteDialog.setTitle(UiUtil.getString(R.string.schedule_delete_tip_title))
                            .setHintTip(UiUtil.getString(R.string.schedule_delete_tip_hint))
                            .setLeftText(UiUtil.getString(R.string.cancel_)).setRightText(UiUtil.getString(R.string.dialog_del_confirm))
                            .setOnRightButtonClck(() -> {
                                recyclerView.closeMenu();
                                showLoadingDialog();
                                scheduleBeans.get(clickPosition).reset();
                                setSchedule(scheduleBeans.get(clickPosition));
                            });
                }
                if (!deleteDialog.isAdded()) {
                    deleteDialog.show(getSupportFragmentManager(), "delete_schedule");
                }
            }

        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initData() {
        super.initData();
        weakHandler = new WeakHandler(msg -> {
            getClockInfo();
            return false;
        });
//        LiveEventBus.get(EnvConfigure.KEY_SCHEDULE, ScheduleBean.class).observe(this, bean -> {
//            if (adapter != null) {
//                ScheduleBean b;
//                boolean isHave = false;
//                for (int i = 0; i < scheduleBeans.size(); i++) {
//                    b = scheduleBeans.get(i);
//                    if (b.getKeyIndex() == bean.getKeyIndex()) {
//                        isHave = true;
//                        if (bean.getWeek() == 0) {//删除
//                            scheduleBeans.remove(i);
//                            adapter.notifyDataSetChanged();
//                        } else {
//                            b.copy(bean);
//                            adapter.notifyDataSetChanged();
//                        }
//                        break;
//                    }
//                }
//                if (bean.getWeek() != 0 && !isHave) {
//                    scheduleBeans.add(bean);
//                    adapter.notifyDataSetChanged();
//                }
//            }
//        });
    }

    @OnClick(R.id.ivb_add_clock)
    public void onClick(View view) {
        if (scheduleBeans.size() == 7) {
            ToastUtils.showToast(UiUtil.getString(R.string.schedule_max_num_tip));
        } else {
            int scheduleKeyIndex = 0;
            int[] keyIndex = new int[]{1, 2, 3, 4, 5, 6, 7};
            for (ScheduleBean bean : scheduleBeans) {
                keyIndex[bean.getKeyIndex() - 1] = 0;
            }
            for (int index : keyIndex) {
                if (index != 0) {
                    scheduleKeyIndex = index;
                    break;
                }
            }
            ScheduleBean scheduleBean = new ScheduleBean();
            scheduleBean.setKeyIndex(scheduleKeyIndex);
            scheduleBean.setEnd(300);
            scheduleBean.setEnable(1);
            scheduleBean.setLoop(1);
            scheduleBean.setMode(MsgCodeUtils.STATUE_PLANNING);
            scheduleBean.setWeek(0x80);//默认为仅一次
            Intent intent = new Intent(ClockingActivity.this, ClockEditActivity.class);
            intent.putExtra(ClockEditActivity.KEY_SCHEDULE_INFO, scheduleBean);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getClockInfo();
    }

    /**
     * position 0-6《-----》schedule1-schedule7
     */
    private void setSchedule(ScheduleBean bean) {
        String scheduleJson = JSON.toJSONString(bean);
        JSONObject jsoSchedule = JSONObject.parseObject(scheduleJson);
        String json_str = "{\"Schedule\":\" \"}";
        String schedule_ = json_str.replaceFirst(EnvConfigure.KEY_SCHEDULE, EnvConfigure.KEY_SCHEDULE + bean.getKeyIndex());
        JSONObject jso = JSONObject.parseObject(schedule_);
        jso.put(EnvConfigure.KEY_SCHEDULE + bean.getKeyIndex(), jsoSchedule);
        IlifeAli.getInstance().setProperties(jso, aBoolean -> {
            weakHandler.sendEmptyMessageDelayed(1, 200);
        });
    }

    public void getClockInfo() {
        IlifeAli.getInstance().getScheduleInfo(new OnAliResponse<List<ScheduleBean>>() {
            @Override
            public void onSuccess(List<ScheduleBean> beans) {
                hideLoadingDialog();
                scheduleBeans.clear();
                scheduleBeans.addAll(beans);
                adapter.notifyDataSetChanged();
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
            }

            @Override
            public void onFailed(int code, String message) {
                hideLoadingDialog();
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                ToastUtils.showErrorToast(context, code);
            }
        });
    }
}
