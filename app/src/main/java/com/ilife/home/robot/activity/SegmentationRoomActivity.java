package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliResponseSingle;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.model.bean.VirtualWallBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SegmentationRoomActivity extends BackBaseActivity {
    private final String TAG = "SelectRoomActivity";
    public static final String KEY_MAP_ID = "map_id";
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.map_segmentation_room)
    MapView map_room;
    @BindView(R.id.tv_move_map)
    TextView tv_move_map;

    @BindView(R.id.rg_segmentation_room)
    RadioGroup rg_segmentation_room;

    @BindView(R.id.fl_top_menu)
    FrameLayout fl_top_menu;
    @BindView(R.id.image_back)
    ImageView iv_back;
    @BindView(R.id.image_menu)
    ImageView iv_finish;
    private int mapId;

    @Override
    public int getLayoutId() {
        return R.layout.activity_segmentation_room;
    }

    @Override
    public void initView() {
        tv_title.setText("分割房间");
        iv_back.setImageResource(R.drawable.nav_button_cancel);
        iv_finish.setImageResource(R.drawable.nav_button_finish);
        fl_top_menu.setVisibility(View.VISIBLE);
        rg_segmentation_room.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.tv_move_map:
                    map_room.setmOT(MapView.OT.MAP);
                    break;
                case R.id.tv_segmentation_room://分割房间
                    map_room.setmOT(MapView.OT.SEGMENT_ROOM);
                    map_room.getmGateHelper().revertGate();
                    break;
                case R.id.tv_combine_room://合并房间
                    map_room.setmOT(MapView.OT.MERGE_ROOM);
                    map_room.getmRoomHelper().cleanSelectRoom();
                    map_room.getmSegmentHelper().setHaveLine(false);
                    map_room.getmSegmentHelper().setGateEffective(false);
                    break;
            }
            map_room.invalidateUI();
        });
        rg_segmentation_room.check(R.id.tv_move_map);
    }

    @Override
    public void initData() {
        super.initData();
        mapId = getIntent().getIntExtra(KEY_MAP_ID, 0);
        if (mapId != 0) {
            IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
                @Override
                public void onSuccess(PropertyBean bean) {
                    IlifeAli.getInstance().getSelectMap(mapId, new OnAliResponse<List<HistoryRecordBean>>() {
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
                            String saveMapDataInfoKey = "";
                            if (mapId == bean.getSaveMapDataInfoMapId1()) {
                                saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO1;
                            } else if (mapId == bean.getSaveMapDataInfoMapId2()) {
                                saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO2;
                            } else if (mapId == bean.getSaveMapDataInfoMapId3()) {
                                saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO3;
                            }
                            if (!TextUtils.isEmpty(saveMapDataInfoKey)) {
                                IlifeAli.getInstance().getSaveMapDataInfo(mapId, saveMapDataInfoKey, new OnAliResponse<String[]>() {
                                    @Override
                                    public void onSuccess(String[] result) {
                                        if (result != null && result.length > 0) {
                                            parseMapInfo(result);
                                        }
                                    }

                                    @Override
                                    public void onFailed(int code, String message) {

                                    }
                                });
                            }

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
    }


    @OnClick({R.id.fl_top_menu})
    public void onClick(View view) {
        //TODO 发送合并房间数据
        if (map_room.getmOT() == MapView.OT.SEGMENT_ROOM) {
            String json = "{\"AddRoomDoor\":{\"ModifyResult\":0,\"CmdId\":1588726604,\"ModifyInfo\":\"AAAAAf///+v//wAE\",\"MapId\":1588571932}}";
            JSONObject jsonObject = JSONObject.parseObject(json);
            jsonObject.getJSONObject("AddRoomDoor").put("MapId", mapId);
            jsonObject.getJSONObject("AddRoomDoor").put("CmdId", (int) (System.currentTimeMillis() / 1000f));
            jsonObject.getJSONObject("AddRoomDoor").put("ModifyInfo", map_room.getmSegmentHelper().getSegmentationData());
            IlifeAli.getInstance().setProperties(jsonObject, aBoolean -> {
                ToastUtils.showToast(aBoolean ? "添加门成功" : "添加门失败");
            });
        } else {
            String json = "{\"DeleteRoomDoor\":{\"ModifyResult\":0,\"CmdId\":1588726604,\"ModifyInfo\":\"AAAAAf///+v//wAE\",\"MapId\":1588571932}}";
            JSONObject jsonObject = JSONObject.parseObject(json);
            jsonObject.getJSONObject("DeleteRoomDoor").put("MapId", mapId);
            jsonObject.getJSONObject("DeleteRoomDoor").put("CmdId", (int) (System.currentTimeMillis() / 1000f));
            jsonObject.getJSONObject("DeleteRoomDoor").put("ModifyInfo", map_room.getmGateHelper().getDeleteGate());
            IlifeAli.getInstance().setProperties(jsonObject, aBoolean -> {
                ToastUtils.showToast(aBoolean ? "删除房间门成功" : "删除房间门失败");
            });
        }

    }

    private void parseMapInfo(String[] data) {
        List<byte[]> bytesList = new ArrayList<>();
        int bytesNumber = 0;
        for (int i = 0; i < data.length; i++) {
            byte[] bytes = Base64.decode(data[i], Base64.DEFAULT);
            bytesNumber += bytes.length;
            bytesList.add(bytes);
        }
        byte[] allBytes = new byte[bytesNumber];
        int desPos = 0;
        for (byte[] b : bytesList) {
            System.arraycopy(b, 0, allBytes, desPos, b.length);
            desPos += b.length;
        }
        MyLogger.d(TAG, "数据：   " + Arrays.toString(allBytes));
//                充电座位置(4bytes) +
//                房间数(1 byte)+
//                房间 1 ID(4bytes) + 房间 1 坐标(4bytes) +
//                房间 1 墙的坐标数(2bytes) + 房间墙坐标
//                (4 * n bytes) +…+
//                门条数(1 byte)+
//                门 1 ID(1 byte)+门 1 坐标(8 byte)+…+
//                虚拟墙条数(1 byte)+
//                虚拟墙 1 坐标(8 byte)+…+
//                禁区条数(1 byte)+
//                禁区 1 类型(1 byte)+禁区 1 坐标

        int index = 0;
        int chargeX = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
        index += 2;
        int chargeY = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
        index += 2;
        int roomNumber = allBytes[index] & 0xff;
        index++;
        List<PartitionBean> rooms = new ArrayList<>();
        PartitionBean room;
        List<Coordinate> wallCoordinate;
        for (int i = 0; i < roomNumber; i++) {
            int roomId = DataUtils.getRoomId(new byte[]{allBytes[index + 3], allBytes[index + 2], allBytes[index + 1], allBytes[index]});
            index += 4;
            int roomX = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
            index += 2;
            int roomY = -DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
            index += 2;
            int wallNumber = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
            index += 2;
            wallCoordinate = new ArrayList<>();
            for (int j = 0; j < wallNumber; j++) {
                int wallX = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                index += 2;
                int wallY = -DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                index += 2;
                wallCoordinate.add(new Coordinate(wallX, wallY, 2));
            }
            room = new PartitionBean(roomId, roomX, roomY);
            room.setWallCoordinates(wallCoordinate);
            rooms.add(room);
        }
        int gateNumber = allBytes[index] & 0xff;
        index++;
        List<VirtualWallBean> gates = new ArrayList<>();
        for (int i = 0; i < gateNumber; i++) {
            int gateId = allBytes[index] & 0xff;
            index++;
            int sx = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
            index += 2;
            int sy = -DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
            index += 2;
            int ex = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
            index += 2;
            int ey = -DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
            index += 2;
            gates.add(new VirtualWallBean(gateId, 4, new float[]{sx, sy, ex, ey}, 1));
        }
        map_room.getmRoomHelper().drawRoom(rooms);
        map_room.getmRoomHelper().setSingleChoice(true);
        map_room.getmGateHelper().drawGate(gates);
        map_room.invalidateUI();
    }


}
