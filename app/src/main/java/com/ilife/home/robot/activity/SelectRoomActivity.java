package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
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

public class SelectRoomActivity extends BaseSlamMapActivity {
    private final String TAG = "SelectRoomActivity";
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.map_room)
    MapView map_room;
    @BindView(R.id.tv_move_map)
    TextView tv_move_map;
    @BindView(R.id.tv_select_room)
    TextView tv_select_room;

    @BindView(R.id.rg_select_room)
    RadioGroup rg_select_room;

    @BindView(R.id.fl_top_menu)
    FrameLayout fl_top_menu;
    @BindView(R.id.image_back)
    ImageView iv_back;
    @BindView(R.id.image_menu)
    ImageView iv_finish;
    @BindView(R.id.iv_clean_room_time)
    ImageView iv_clean_room_time;
    private int times = 1;//清扫次数
    private HashMap<String, String> roomNames = new HashMap<>();

    @Override
    public int getLayoutId() {
        return R.layout.activity_select_room;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.map_bottom_sheet_select_room);
        iv_back.setImageResource(R.drawable.nav_button_cancel);
        iv_finish.setImageResource(R.drawable.nav_button_finish);
        fl_top_menu.setVisibility(View.VISIBLE);
        rg_select_room.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.tv_move_map:
                    map_room.setmOT(MapView.OT.MAP);
                    break;
                case R.id.tv_select_room:
                    map_room.setmOT(MapView.OT.SELECT_ROOM);
                    break;
            }
        });
        rg_select_room.check(R.id.tv_move_map);
    }

    @Override
    protected void onSaveMapBean() {
        long mapId = propertyBean.getSelectedMapId();
        boolean isParseRoom = DataUtils.parseRoomInfo(String.valueOf(mapId), propertyBean.getMapRoomInfo1(), roomNames);
        if (!isParseRoom) {
            isParseRoom = DataUtils.parseRoomInfo(String.valueOf(mapId), propertyBean.getMapRoomInfo2(), roomNames);
        }
        if (!isParseRoom) {
            DataUtils.parseRoomInfo(String.valueOf(mapId), propertyBean.getMapRoomInfo3(), roomNames);
        }

        MapDataBean mapDataBean = DataUtils.parseSaveMapData(saveMapBean.getMapData());
        if (mapDataBean != null) {
            map_room.setLeftTopCoordinate(mapDataBean.getLeftX(), mapDataBean.getLeftY());
            map_room.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
            map_room.drawMapX8(mapDataBean.getCoordinates());
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
            map_room.drawChargePort(saveMapDataInfoBean.getChargePoint().x, saveMapDataInfoBean.getChargePoint().y, true);
            map_room.getmGateHelper().drawGate(saveMapDataInfoBean.getGates());
            map_room.getmRoomHelper().drawRoom(saveMapDataInfoBean.getRooms());
            map_room.invalidateUI();
        }
    }

    @Override
    public void initData() {
        super.initData();
    }

    @OnClick({R.id.fl_top_menu, R.id.iv_clean_room_time})
    public void onClick(View view) {
        if (view.getId() == R.id.iv_clean_room_time) {
            if (times == 3) {
                times = 0;
            }
            times++;

            updateLoopImage();
        } else {
            String roomData = "{\"CleanPartitionData\":{\"CleanLoop\":1,\"Enable\":1,\"PartitionData\":0}}";
            JSONObject caJson = JSONObject.parseObject(roomData);
            int room = map_room.getSelectRoom();
            if (room == 0) {
                ToastUtils.showToast(UiUtil.getString(R.string.toast_select_one_room));
            } else {
                caJson.getJSONObject(EnvConfigure.CleanPartitionData).put("PartitionData", room);
                caJson.getJSONObject(EnvConfigure.CleanPartitionData).put("CleanLoop", times);
                MyLogger.d(TAG, "选房清扫：" + caJson.toString());
                IlifeAli.getInstance().setProperties(caJson, aBoolean -> {
                    if (aBoolean) {
                        ToastUtils.showToast(UiUtil.getString(R.string.setting_success));
                        finish();
                    }
                });
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
        ToastUtils.showCleanTimes(times);
        iv_clean_room_time.setImageResource(src);
    }

}
