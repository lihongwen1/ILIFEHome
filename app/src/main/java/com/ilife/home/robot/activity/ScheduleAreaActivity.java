package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.bean.ScheduleBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.bean.SaveMapDataInfoBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.view.MapView;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 选择预约区域
 */
public class ScheduleAreaActivity extends BaseSlamMapActivity {
    private static final String TAG = "ScheduleAreaActivity";
    public static final String KEY_SCHEDULE_AREA_BEAN = "schedule_area_bean";
    @BindView(R.id.rg_schedule_area)
    RadioGroup rg_schedule_area;
    @BindView(R.id.map_schedule_area)
    MapView map_schedule_area;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.fl_top_menu)
    FrameLayout fl_top_menu;
    @BindView(R.id.image_back)
    ImageView iv_back;
    @BindView(R.id.image_menu)
    ImageView iv_finish;
    private ScheduleBean scheduleBean;
    @BindView(R.id.iv_schedule_clean_time)
    ImageView iv_schedule_clean_time;
    @BindView(R.id.fl_no_map)
    FrameLayout fl_no_map;
    private int times;
    private HashMap<String, String> roomNames = new HashMap<>();

    @Override
    public int getLayoutId() {
        return R.layout.activity_shedule_area;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.clock_edit_choose_area);
        iv_back.setImageResource(R.drawable.nav_button_cancel);
        iv_finish.setImageResource(R.drawable.nav_button_finish);
        fl_top_menu.setVisibility(View.VISIBLE);

        rg_schedule_area.setOnCheckedChangeListener((group, checkedId) -> {
            if (fl_no_map.getVisibility() == View.VISIBLE) {
                rg_schedule_area.check(R.id.tv_schedule_area);
                ToastUtils.showToast(UiUtil.getString(R.string.map_tip_no_map_yet));
            } else {
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
                        updateLoopImage(false);
                        break;
                    case R.id.tv_schedule_area_clean_area:
                        map_schedule_area.setmOT(MapView.OT.CLEAN_AREA);
                        map_schedule_area.invalidateUI();
                        iv_schedule_clean_time.setVisibility(View.VISIBLE);
                        updateLoopImage(false);
                        break;
                }
            }
        });
        switch (scheduleBean.getType()) {
            case 0:
                rg_schedule_area.check(R.id.tv_schedule_area);
                break;
            case 1:
                rg_schedule_area.check(R.id.tv_schedule_area_clean_area);
                break;
            case 2:
                rg_schedule_area.check(R.id.tv_schedule_area_room);
                break;
        }
    }

    @Override
    protected void onSaveMapBean() {
        long mapId = propertyBean.getSelectedMapId();
        if (mapId == 0) {
            fl_no_map.setVisibility(View.VISIBLE);
            return;
        }
        DataUtils.parseRoomInfo(String.valueOf(mapId), SpUtils.getSpString(ScheduleAreaActivity.this, "ROOM_NAME"), roomNames);
        boolean isParseRoom = DataUtils.parseRoomInfo(String.valueOf(mapId), propertyBean.getMapRoomInfo1(), roomNames);
        if (!isParseRoom) {
            isParseRoom = DataUtils.parseRoomInfo(String.valueOf(mapId), propertyBean.getMapRoomInfo2(), roomNames);
        }
        if (!isParseRoom) {
            DataUtils.parseRoomInfo(String.valueOf(mapId), propertyBean.getMapRoomInfo3(), roomNames);
        }

        if (isDestroyed() || map_schedule_area == null) {
            return;
        }
        if (saveMapBean.getMapData() == null || saveMapBean.getMapData().length == 0) {
            fl_no_map.setVisibility(View.VISIBLE);
            return;
        }
        MapDataBean mapDataBean = DataUtils.parseSaveMapData(saveMapBean.getMapData());
        if (mapDataBean != null) {
            map_schedule_area.setLeftTopCoordinate(mapDataBean.getLeftX(), mapDataBean.getLeftY());
            map_schedule_area.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
            map_schedule_area.drawMapX8(mapDataBean.getCoordinates());
        }
        String cleanAreaData = scheduleBean.getType() == 1 ? scheduleBean.getArea() : "";
        if (!TextUtils.isEmpty(cleanAreaData)) {
            map_schedule_area.drawCleanArea(cleanAreaData);
        }
        SaveMapDataInfoBean saveMapDataInfoBean = DataUtils.parseSaveMapInfo(saveMapBean.getMapDataInfo());
        if (saveMapDataInfoBean != null) {
            for (PartitionBean room : saveMapDataInfoBean.getRooms()) {//绑定设置的用户名
                String roomName = roomNames.get(String.valueOf(room.getPartitionId()));
                if (roomName == null) {
                    roomName = "";
                }
                room.setTag(roomName);
            }
            map_schedule_area.drawChargePort(saveMapDataInfoBean.getChargePoint().x, saveMapDataInfoBean.getChargePoint().y, true);
            map_schedule_area.getmGateHelper().drawGate(saveMapDataInfoBean.getGates());
            map_schedule_area.getmRoomHelper().drawRoom(saveMapDataInfoBean.getRooms(),scheduleBean.getRoom());
            map_schedule_area.invalidateUI();
        }
    }

    @Override
    public void initData() {
        scheduleBean = getIntent().getParcelableExtra(ClockEditActivity.KEY_SCHEDULE_INFO);
        times = scheduleBean.getLoop();
        super.initData();
    }


    @OnClick({R.id.fl_top_menu, R.id.iv_schedule_clean_time})
    public void onClick(View view) {
        if (view.getId() == R.id.iv_schedule_clean_time) {
            if (times == 3) {
                times = 0;
            }
            times++;
            updateLoopImage(true);
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
                        ToastUtils.showToast(UiUtil.getString(R.string.toast_set_clean_area_first));
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
    private void updateLoopImage(boolean isFromUser) {
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
        if (isFromUser) {
            ToastUtils.showCleanTimes(times);
        }
        iv_schedule_clean_time.setImageResource(src);
    }

}
