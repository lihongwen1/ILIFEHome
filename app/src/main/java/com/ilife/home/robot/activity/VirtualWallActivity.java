package com.ilife.home.robot.activity;

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
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.view.MapView;

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
    private String str_globalArea;

    @Override
    public int getLayoutId() {
        return R.layout.activity_virtual_wall;
    }

    @Override
    public void initView() {
        tv_title.setText("虚拟墙/禁区");
        iv_back.setImageResource(R.drawable.nav_button_cancel);
        iv_finish.setImageResource(R.drawable.nav_button_finish);
        fl_top_menu.setVisibility(View.VISIBLE);
        mMapView.setmOT(MapView.OT.MAP);
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
        });

    }

    @Override
    public void initData() {
        super.initData();
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean result) {
                long selectId = result.getSelectedMapId();
                str_virtual = result.getVirtualWall();
                MyLogger.d(TAG, "服务器虚拟墙数据：" + str_virtual);
                str_mopArea = result.getForbiddenArea();
                MyLogger.d(TAG, "服务器禁区数据：" + str_mopArea);
                IlifeAli.getInstance().getSelectMap(selectId, new OnAliResponse<List<HistoryRecordBean>>() {
                    @Override
                    public void onSuccess(List<HistoryRecordBean> result) {
                        if (result.size() == 0) {//应该只有一条数据
                            return;
                        }
                        MapDataBean mapDataBean = DataUtils.parseSaveMapData(result.get(0).getMapDataArray());
                        if (mapDataBean != null) {
                            mMapView.setLeftTopCoordinate(mapDataBean.getLeftX(), mapDataBean.getLeftY());
                            mMapView.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
                            mMapView.drawMapX8(mapDataBean.getCoordinates());
                        }
                        mMapView.drawVirtualWall(str_virtual);
                        mMapView.drawForbiddenArea(str_mopArea);
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

    @OnClick({R.id.fl_top_menu})
    public void onClick(View view) {
        if (view.getId() == R.id.fl_top_menu) {
            MyLogger.d(TAG, "虚拟墙数据" + mMapView.getVirtualWallPointfs());
            MyLogger.d(TAG, "全局禁区数据" + mMapView.getForbiddenData());
            String vrData = "{\"VirtualWallData\":\"\"}";
            String parData = "{\"ForbiddenAreaData\":\"\"}";
            JSONObject vrJson = JSONObject.parseObject(vrData);
            vrJson.put(EnvConfigure.VirtualWallData, mMapView.getVirtualWallPointfs());
            IlifeAli.getInstance().setProperties(vrJson, aBoolean -> {
                ToastUtils.showToast("设置虚拟墙：" + (aBoolean ? "成功" : "失败"));
            });
            JSONObject parJson = JSONObject.parseObject(parData);
            parJson.put(EnvConfigure.KEY_FORBIDDEN_AREA, mMapView.getForbiddenData());
            IlifeAli.getInstance().setProperties(parJson, aBoolean -> {
                ToastUtils.showToast("设置禁区：" + (aBoolean ? "成功" : "失败"));
            });
        }
    }

}
