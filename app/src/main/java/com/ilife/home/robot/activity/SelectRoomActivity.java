package com.ilife.home.robot.activity;

import android.view.View;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.view.MapView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SelectRoomActivity extends BackBaseActivity {
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.map_room)
    MapView map_room;
    private String roomData;
    @BindView(R.id.tv_name_room)
    TextView tv_name_room;
    @BindView(R.id.tv_select_room)
    TextView tv_select_room;
    @Override
    public int getLayoutId() {
        return R.layout.activity_select_room;
    }

    @Override
    public void initView() {
        tv_title.setText("选房清扫");
        map_room.setmOT(MapView.OT.SELECT_ROOM);
    }

    @Override
    public void initData() {
        super.initData();
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean result) {
                long selectId = result.getSelectedMapId();
                roomData = result.getPartition();
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


    @OnClick({R.id.tv_name_room,R.id.tv_select_room})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_name:
                ToastUtils.showToast("开发中");
                break;
            case R.id.tv_select_room:
                map_room.setmOT(MapView.OT.SELECT_ROOM);
                break;
        }
    }


}
