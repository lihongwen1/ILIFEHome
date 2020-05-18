package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.view.KeyEvent;
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
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.bean.SaveMapDataInfoBean;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.model.MapX9Model;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.view.MapView;
import com.ilife.home.robot.view.helper.ForbiddenAreaHelper;
import com.ilife.home.robot.view.helper.VirtualWallHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 编辑虚拟墙，禁区的界面
 */
public class VirtualWallActivity extends BackBaseActivity {
    private static final String TAG = "VirtualWallActivity";
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.map_edit_vw)
    MapView mMapView;
    @BindView(R.id.fl_top_menu)
    FrameLayout fl_top_menu;
    @BindView(R.id.image_back)
    ImageView iv_back;
    @BindView(R.id.image_menu)
    ImageView iv_finish;
    @BindView(R.id.rg_vw_fbd)
    RadioGroup rg_vw_fbd;
    private String str_virtual;
    private String str_mopArea;
    private String charging_port;
    List<Coordinate> coordinates = new ArrayList<>();

    @Override
    public int getLayoutId() {
        return R.layout.activity_virtual_wall;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.map_bottom_sheet_vir_fbd);
        iv_back.setImageResource(R.drawable.nav_button_cancel);
        iv_finish.setImageResource(R.drawable.nav_button_finish);
        fl_top_menu.setVisibility(View.VISIBLE);
        rg_vw_fbd.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.tv_move_map:
                    mMapView.setmOT(MapView.OT.MAP);
                    break;
                case R.id.tv_virtual:
                    mMapView.setmOT(MapView.OT.VIRTUAL_WALL);
                    break;
                case R.id.tv_mop_area:
                    mMapView.setmOT(MapView.OT.MOP_FORBIDDEN_AREA);
                    break;
                case R.id.map_global_area:
                    mMapView.setmOT(MapView.OT.GLOBAL_FORBIDDEN_AREA);
                    break;
            }
            mMapView.invalidateUI();
        });
        rg_vw_fbd.check(R.id.tv_move_map);
    }

    @Override
    public void initData() {
        super.initData();
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean bean) {
                boolean isDrawSaveMap = false;
                int workMode = bean.getWorkMode();
                if (workMode == MsgCodeUtils.STATUE_CHARGING || workMode == MsgCodeUtils.STATUE_CHARGING_BASE_SLEEP) {
                    isDrawSaveMap = true;
                } else {
                    isDrawSaveMap = bean.isInitStatus();
                }
                long mapId = bean.getSelectedMapId();
                str_virtual = bean.getVirtualWall();
                MyLogger.d(TAG, "服务器虚拟墙数据：" + str_virtual);
                str_mopArea = bean.getForbiddenArea();
                charging_port = bean.getChargePort();
                MyLogger.d(TAG, "服务器禁区数据：" + str_mopArea);

                MapX9Model model = new MapX9Model();
                model.queryHistoryData(bean.getRealTimeMapTimeLine(), cleaningDataX8 -> {
                    MyLogger.d(TAG, "getHistoryDataX8-------------------------------success");
                    drawMap(cleaningDataX8.getCoordinates());
                });
                if (isDrawSaveMap && mapId != 0) {
                    String saveMapDataKey = "";
                    if (mapId == bean.getSaveMapDataMapId1()) {
                        saveMapDataKey = EnvConfigure.KEY_SAVE_MAP_DATA_1;
                    } else if (mapId == bean.getSaveMapDataMapId2()) {
                        saveMapDataKey = EnvConfigure.KEY_SAVE_MAP_DATA_2;
                    } else if (mapId == bean.getSaveMapDataMapId3()) {
                        saveMapDataKey = EnvConfigure.KEY_SAVE_MAP_DATA_3;
                    }
                    IlifeAli.getInstance().getSaveMapData(mapId, saveMapDataKey, new OnAliResponse<String[]>() {
                        @Override
                        public void onSuccess(String[] result) {
                            if (result.length == 0) {//应该只有一条数据
                                return;
                            }
                            MapDataBean mapDataBean = DataUtils.parseSaveMapData(result);
                            if (mapDataBean != null) {
                                drawMap(mapDataBean.getCoordinates());
                            }
                            String saveMapDataInfoKey = "";
                            if (mapId == bean.getSaveMapDataInfoMapId1()) {
                                saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO1;
                            } else if (mapId == bean.getSaveMapDataInfoMapId2()) {
                                saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO2;
                            } else if (mapId == bean.getSaveMapDataInfoMapId3()) {
                                saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO3;
                            }
                            fetchSaveMapDataInfo(saveMapDataInfoKey, (int) mapId);

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


    private void fetchSaveMapDataInfo(String saveMapDataInfoKey, int mapId) {
        if (!TextUtils.isEmpty(saveMapDataInfoKey)) {
            IlifeAli.getInstance().getSaveMapDataInfo(mapId, saveMapDataInfoKey, new OnAliResponse<String[]>() {
                @Override
                public void onSuccess(String[] result) {
                    if (result != null && result.length > 0) {
                        SaveMapDataInfoBean saveMapDataInfoBean = DataUtils.parseSaveMapInfo(result);
                        mMapView.drawChargePort(saveMapDataInfoBean.getChargePoint().x, saveMapDataInfoBean.getChargePoint().y, true);
                        mMapView.getmGateHelper().drawGate(saveMapDataInfoBean.getGates());
                        mMapView.invalidateUI();
                    }
                }

                @Override
                public void onFailed(int code, String message) {

                }
            });
        }
    }


    private synchronized void drawMap(List<Coordinate> data) {
        coordinates.addAll(data);
        updateSlamX8(coordinates, 0);
        mMapView.drawMapX8(coordinates);
        mMapView.drawVirtualWall(str_virtual);
        mMapView.drawForbiddenArea(str_mopArea);
        /**
         * 处理充电座
         */
        if (!TextUtils.isEmpty(charging_port)) {
            JSONObject jsonObject = JSONObject.parseObject(charging_port);
            boolean isDisplay = jsonObject.getIntValue("DisplaySwitch") == 1;
            if (isDisplay) {
                int xy = jsonObject.getIntValue("Piont");
                byte[] bytes = DataUtils.intToBytes4(xy);
                int x = DataUtils.bytesToInt(new byte[]{bytes[0], bytes[1]}, 0);
                int y = -DataUtils.bytesToInt(new byte[]{bytes[2], bytes[3]}, 0);
                mMapView.drawChargePort(x, y,true);
            }
        }
    }

    private void updateSlamX8(List<Coordinate> src, int offset) {
        int minX;
        int maxX;
        int minY;
        int maxY;
        if (src == null || src.size() < 2) {
            return;
        }
        Coordinate coordinate;
        coordinate = src.get(0);
        minX = coordinate.getX();
        minY = coordinate.getY();
        maxX = coordinate.getX();
        maxY = coordinate.getY();
        MyLogger.d(TAG, "data is  clear, and  need to reset all params");
        int x, y;
        for (int i = 0; i < src.size(); i++) {
            coordinate = src.get(i);
            x = coordinate.getX();
            y = coordinate.getY();
            if (minX > x) {
                minX = x;
            }
            if (maxX < x) {
                maxX = x;
            }
            if (minY > y) {
                minY = y;
            }
            if (maxY < y) {
                maxY = y;
            }
        }
        mMapView.updateSlam(minX, maxX, minY, maxY);
    }

    /**
     * //todo 虚拟墙和禁区保存时，虚拟墙大概率会失败
     *
     * @param view
     */
    @OnClick({R.id.fl_top_menu, R.id.image_back})
    public void onClick(View view) {
        if (view.getId() == R.id.fl_top_menu) {
            VirtualWallHelper virHelper = mMapView.getmVirtualWallHelper();
            ForbiddenAreaHelper fbdHelper = mMapView.getmForbiddenHelper();
            if (!virHelper.isClose() && !fbdHelper.isClose()) {
                String vrData = "{\"VirtualWallData\":\"\"}";
                String parData = "{\"ForbiddenAreaData\":\"\"}";
                JSONObject vrJson = JSONObject.parseObject(vrData);
                if (mMapView != null) {
                    vrJson.put(EnvConfigure.VirtualWallData, mMapView.getVirtualWallPointfs());
                    JSONObject parJson = JSONObject.parseObject(parData);
                    parJson.put(EnvConfigure.KEY_FORBIDDEN_AREA, mMapView.getForbiddenData());
                    IlifeAli.getInstance().setProperties(vrJson, aBoolean -> {
                        if (aBoolean) {
                            IlifeAli.getInstance().setProperties(parJson, result -> {
                                if (result) {
                                    ToastUtils.showToast(UiUtil.getString(R.string.setting_success));
                                    removeActivity();
                                }
                            });
                        }
                    });
                }
            }


        }
        if (view.getId() == R.id.image_back) {
            showAbandonDialog();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showAbandonDialog();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void showAbandonDialog() {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setTitle(UiUtil.getString(R.string.abandom_operation_title))
                .setHintTip("")
                .setLeftText(UiUtil.getString(R.string.abandom)).setRightText(UiUtil.getString(R.string.continue_operation))
                .setOnRightButtonClck(() -> {
                    if (str_virtual != null) {
                        String vrData = "{\"VirtualWallData\":\"\"}";
                        JSONObject vrJson = JSONObject.parseObject(vrData);
                        vrJson.put(EnvConfigure.VirtualWallData, str_virtual);
                        IlifeAli.getInstance().setProperties(vrJson, aBoolean -> {
                            finish();
                        });
                    }
                });
        universalDialog.show(getSupportFragmentManager(), "abandon_operation");
    }
}
