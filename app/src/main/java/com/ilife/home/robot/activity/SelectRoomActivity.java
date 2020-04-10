package com.ilife.home.robot.activity;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.view.MapView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SelectRoomActivity extends BackBaseActivity {
    private final String TAG = "SelectRoomActivity";
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.map_room)
    MapView map_room;
    @BindView(R.id.tv_name_room)
    TextView tv_name_room;
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
    private int times=1;//清扫次数

    @Override
    public int getLayoutId() {
        return R.layout.activity_select_room;
    }

    @Override
    public void initView() {
        tv_title.setText("选房清扫");
        iv_back.setImageResource(R.drawable.nav_button_cancel);
        iv_finish.setImageResource(R.drawable.nav_button_finish);
        fl_top_menu.setVisibility(View.VISIBLE);
        map_room.setmOT(MapView.OT.SELECT_ROOM);
        rg_select_room.setOnCheckedChangeListener((group, checkedId) -> {

        });
        rg_select_room.check(R.id.tv_select_room);
    }

    @Override
    public void initData() {
        super.initData();
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean result) {
                long selectId = result.getSelectedMapId();
                String roomData = result.getPartition();
                IlifeAli.getInstance().getSelectMap(selectId, new OnAliResponse<List<HistoryRecordBean>>() {
                    @Override
                    public void onSuccess(List<HistoryRecordBean> result) {
                        if (result.size() == 0) {//应该只有一条数据
                            return;
                        }
                        MapDataBean mapDataBean = DataUtils.parseSaveMapData(result.get(0).getMapDataArray());
                        if (mapDataBean != null) {
                            map_room.setLeftTopCoordinate(mapDataBean.getLeftX(), mapDataBean.getLeftY());
                            map_room.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
                            map_room.drawMapX8(mapDataBean.getCoordinates());
                        }
                        map_room.drawRoomTag(roomData);

                    }

                    @Override
                    public void onFailed(int code, String message) {

                    }
                });
            }

            @Override
            public void onFailed(int code, String message) {

            }
        });
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
            caJson.getJSONObject(EnvConfigure.CleanPartitionData).put("PartitionData", map_room.getSelectRoom());
            caJson.getJSONObject(EnvConfigure.CleanPartitionData).put("CleanLoop",times);
            MyLogger.d(TAG, "选房清扫：" + caJson.toString());
            IlifeAli.getInstance().setProperties(caJson, aBoolean -> {
                if (aBoolean) {
                    ToastUtils.showToast("设置选房清扫数据成功");
                    finish();
                }
            });
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
        iv_clean_room_time.setImageResource(src);
    }

}
