package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.bean.ScheduleBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.view.MapView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 选择预约区域
 */
public class ScheduleAreaActivity extends BackBaseActivity {
    private static final String TAG = "ScheduleAreaActivity";
    public static final String KEY_SCHEDULE_AREA_BEAN = "schedule_area_bean";
    @BindView(R.id.rg_schedule_area)
    RadioGroup rg_schedule_area;
    @BindView(R.id.map_schedule_area)
    MapView map_schedule_area;

    @BindView(R.id.fl_top_menu)
    FrameLayout fl_top_menu;
    @BindView(R.id.image_back)
    ImageView iv_back;
    @BindView(R.id.image_menu)
    ImageView iv_finish;
    private ScheduleBean scheduleBean;
    @BindView(R.id.iv_schedule_clean_time)
    ImageView iv_schedule_clean_time;
    private int times;

    @Override
    public int getLayoutId() {
        return R.layout.activity_shedule_area;
    }

    @Override
    public void initView() {
        iv_back.setImageResource(R.drawable.nav_button_cancel);
        iv_finish.setImageResource(R.drawable.nav_button_finish);
        fl_top_menu.setVisibility(View.VISIBLE);
        rg_schedule_area.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.tv_schedule_area:
                    map_schedule_area.setmOT(MapView.OT.NOON);
                    map_schedule_area.invalidateUI();
                    iv_schedule_clean_time.setVisibility(View.GONE);
                    break;
                case R.id.tv_schedule_area_room:
                    map_schedule_area.setmOT(MapView.OT.SELECT_ROOM);
                    map_schedule_area.invalidateUI();
                    iv_schedule_clean_time.setVisibility(View.VISIBLE);
                    times = 1;
                    updateLoopImage();
                    break;
                case R.id.tv_schedule_area_clean_area:
                    map_schedule_area.setmOT(MapView.OT.CLEAN_AREA);
                    map_schedule_area.invalidateUI();
                    iv_schedule_clean_time.setVisibility(View.VISIBLE);
                    times = 1;
                    updateLoopImage();
                    break;
            }
        });
        rg_schedule_area.check(R.id.tv_schedule_area);
    }

    @Override
    public void initData() {
        scheduleBean = getIntent().getParcelableExtra(ClockEditActivity.KEY_SCHEDULE_INFO);
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean result) {
                long selectId = result.getSelectedMapId();
                String partitionData = result.getPartition();
                IlifeAli.getInstance().getSelectMap(selectId, new OnAliResponse<List<HistoryRecordBean>>() {
                    @Override
                    public void onSuccess(List<HistoryRecordBean> result) {
                        if (result.size() == 0) {//应该只有一条数据
                            return;
                        }
                        MapDataBean mapDataBean = DataUtils.parseSaveMapData(result.get(0).getMapDataArray());
                        if (mapDataBean != null) {
                            map_schedule_area.setLeftTopCoordinate(mapDataBean.getLeftX(), mapDataBean.getLeftY());
                            map_schedule_area.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
                            map_schedule_area.drawMapX8(mapDataBean.getCoordinates());
                        }
                        if (!TextUtils.isEmpty(partitionData)) {
                            map_schedule_area.getmRoomHelper().drawRoom(partitionData, scheduleBean.getType() == 2 ? scheduleBean.getRoom() : 0);
                        }
                        String cleanAreaData = scheduleBean.getType() == 1 ? scheduleBean.getArea() : "";
                        if (!TextUtils.isEmpty(cleanAreaData)) {
                            map_schedule_area.drawCleanArea(cleanAreaData);
                        }
                    }

                    @Override
                    public void onFailed(int code, String message) {

                    }
                });
            }

            @Override
            public void onFailed(int code, String message) {
                MyLogger.e(TAG, "获取清扫区域数据失败，reason: " + message);
            }
        });

    }

    @OnClick({R.id.fl_top_menu, R.id.iv_schedule_clean_time})
    public void onClick(View view) {
        if (view.getId() == R.id.iv_schedule_clean_time) {
            if (times == 3) {
                times = 0;
            }
            times++;
            updateLoopImage();
        } else {
            ScheduleBean scheduleBean = new ScheduleBean();
            scheduleBean.setLoop(times);
            switch (map_schedule_area.getmOT()) {
                case NOON:
                    scheduleBean.setType(0);
                    LiveEventBus.get(KEY_SCHEDULE_AREA_BEAN, ScheduleBean.class).post(scheduleBean);
                    removeActivity();
                    break;
                case SELECT_ROOM:
                    int room = map_schedule_area.getSelectRoom();
                    if (room == 0) {
                        ToastUtils.showToast(UiUtil.getString(R.string.toast_select_one_room));
                    } else {
                        scheduleBean.setType(2);
                        scheduleBean.setRoom(room);
                        LiveEventBus.get(KEY_SCHEDULE_AREA_BEAN, ScheduleBean.class).post(scheduleBean);
                        removeActivity();
                    }
                    break;
                case CLEAN_AREA:
                    String cleanArea = map_schedule_area.getCleanAreaData();
                    if (cleanArea.equals("AAAAAAAAAAAAAAAAAAAAAA==")) {
                        ToastUtils.showToast("请先进行划区");
                    } else {
                        scheduleBean.setType(1);
                        scheduleBean.setArea(cleanArea);
                        LiveEventBus.get(KEY_SCHEDULE_AREA_BEAN, ScheduleBean.class).post(scheduleBean);
                        removeActivity();
                    }
                    break;
            }
        }

    }

    /**
     * 更新循环次数图片
     */
    private void updateLoopImage() {
        int src = R.drawable.operation_btn_fre_1;
        switch (times) {
            case 1:
                src = R.drawable.operation_btn_fre_1;
                break;
            case 2:
                src = R.drawable.operation_btn_fre_2;
                break;
            case 3:
                src = R.drawable.operation_btn_fre_3;
                break;
        }
        iv_schedule_clean_time.setImageResource(src);
    }

}
