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
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.model.MapX9Model;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.view.MapView;

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
                long selectId = bean.getSelectedMapId();
                str_virtual = bean.getVirtualWall();
                MyLogger.d(TAG, "服务器虚拟墙数据：" + str_virtual);
                str_mopArea = bean.getForbiddenArea();
                charging_port = bean.getChagePort();
                MyLogger.d(TAG, "服务器禁区数据：" + str_mopArea);

                MapX9Model model = new MapX9Model();
                model.queryHistoryData(bean.getRealTimeMapTimeLine(), cleaningDataX8 -> {
                    MyLogger.d(TAG, "getHistoryDataX8-------------------------------success");
                    drawMap(cleaningDataX8.getCoordinates());
                });
                IlifeAli.getInstance().getSelectMap(selectId, new OnAliResponse<List<HistoryRecordBean>>() {
                    @Override
                    public void onSuccess(List<HistoryRecordBean> result) {
                        if (result.size() == 0) {//应该只有一条数据
                            return;
                        }
                        MapDataBean mapDataBean = DataUtils.parseSaveMapData(result.get(0).getMapDataArray());
                        if (mapDataBean != null) {
                            drawMap(mapDataBean.getCoordinates());
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

    private synchronized void drawMap(List<Coordinate> data) {
        coordinates.addAll(data);
        updateSlamX8(data, 0);
        mMapView.drawMapX8(data);
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
                mMapView.drawChargePort(x, y);
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
    @OnClick({R.id.fl_top_menu})
    public void onClick(View view) {
        if (view.getId() == R.id.fl_top_menu) {
            String vrData = "{\"VirtualWallData\":\"\"}";
            String parData = "{\"ForbiddenAreaData\":\"\"}";
            JSONObject vrJson = JSONObject.parseObject(vrData);
            vrJson.put(EnvConfigure.VirtualWallData, mMapView.getVirtualWallPointfs());
            IlifeAli.getInstance().setProperties(vrJson, aBoolean -> {
                if (aBoolean) {
                    JSONObject parJson = JSONObject.parseObject(parData);
                    parJson.put(EnvConfigure.KEY_FORBIDDEN_AREA, mMapView.getForbiddenData());
                    IlifeAli.getInstance().setProperties(parJson, result -> {
                        if (result) {
                            removeActivity();
                        }
                    });
                }
            });

        }
    }

    @Override
    protected void beforeFinish() {
        super.beforeFinish();
        if (str_virtual != null) {
            String vrData = "{\"VirtualWallData\":\"\"}";
            JSONObject vrJson = JSONObject.parseObject(vrData);
            vrJson.put(EnvConfigure.VirtualWallData, str_virtual);
            IlifeAli.getInstance().setProperties(vrJson, aBoolean -> {
            });
        }
    }
}
