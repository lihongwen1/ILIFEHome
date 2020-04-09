package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.view.MapView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 选择预约区域
 */
public class ScheduleAreaActivity extends BackBaseActivity {
    private static final String TAG = "ScheduleAreaActivity";
    public static final String LIVE_BUS_SCHEDULE_AREA_DATA="schedule_area_data";
    public static final String LIVE_BUS_SCHEDULE_AREA_TYPE="schedule_area_type";
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
    private String cleanAreaData;
    private String partitionData;

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
                    break;
                case R.id.tv_schedule_area_room:
                    map_schedule_area.setmOT(MapView.OT.SELECT_ROOM);
                    map_schedule_area.invalidateUI();
                    break;
                case R.id.tv_schedule_area_clean_area:
                    map_schedule_area.setmOT(MapView.OT.CLEAN_AREA);
                    map_schedule_area.invalidateUI();
                    break;
            }
        });
        rg_schedule_area.check(R.id.tv_schedule_area);
    }

    @Override
    public void initData() {
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean result) {
                long selectId = result.getSelectedMapId();
                cleanAreaData = result.getCleanArea();
                partitionData = result.getPartition();
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
                            map_schedule_area.drawRoomTag(partitionData);
                        }

                        if (!TextUtils.isEmpty(cleanAreaData)) {
                            JSONObject json = JSON.parseObject(cleanAreaData);
                            boolean enable = json.getIntValue("Enable") == 1;
                            String area = "";
                            if (enable) {
                                area = json.getString("AreaData");
                                if (area.equals("AAAAAAAAAAAAAAAAAAAAAA==")) {
                                    area = "";
                                }
                            }
                            map_schedule_area.drawCleanArea(area);
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

    @OnClick(R.id.fl_top_menu)
    public void onClick(View view) {
        switch (map_schedule_area.getmOT()) {
            case NOON:
                LiveEventBus.get(LIVE_BUS_SCHEDULE_AREA_TYPE,Integer.class)
                        .post(0);
                break;
            case SELECT_ROOM:
                int room=map_schedule_area.getSelectRoom();
                if (room==0){
                    ToastUtils.showToast("请至少选择一个房间");
                }else {
                    LiveEventBus.get(LIVE_BUS_SCHEDULE_AREA_DATA,String.class)
                            .post(String.valueOf(room));
                    LiveEventBus.get(LIVE_BUS_SCHEDULE_AREA_TYPE,Integer.class)
                            .post(2);
                }
                break;
            case CLEAN_AREA:
                String cleanArea=map_schedule_area.getCleanAreaData();
                if (cleanArea.equals("AAAAAAAAAAAAAAAAAAAAAA==")){
                    ToastUtils.showToast("请先进行划区");
                }else {
                    LiveEventBus.get(LIVE_BUS_SCHEDULE_AREA_DATA,String.class)
                            .post(cleanArea);
                    LiveEventBus.get(LIVE_BUS_SCHEDULE_AREA_TYPE,Integer.class)
                            .post(1);
                }
                break;
        }
        removeActivity();
    }
}
