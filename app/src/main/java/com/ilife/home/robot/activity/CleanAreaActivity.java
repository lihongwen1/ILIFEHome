package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
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
 * 划区清扫
 */
public class CleanAreaActivity extends BackBaseActivity {
    private static final String TAG = "CleanAreaActivity";
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.map_clean_area)
    MapView map_clean_area;
    @BindView(R.id.rg_clean_area)
    RadioGroup rg_clean_area;
    @BindView(R.id.iv_clean_area_time)
    ImageView iv_clean_area_time;

    @BindView(R.id.fl_top_menu)
    FrameLayout fl_top_menu;
    @BindView(R.id.image_back)
    ImageView iv_back;
    @BindView(R.id.image_menu)
    ImageView iv_finish;
    private int times;//清扫次数
    private int enable;//0-无效  1-开始 2-进行中

    @Override
    public int getLayoutId() {
        return R.layout.activity_clean_area;
    }

    @Override
    public void initView() {
        tv_title.setText("划区清扫");
        iv_back.setImageResource(R.drawable.nav_button_cancel);
        iv_finish.setImageResource(R.drawable.nav_button_finish);
        fl_top_menu.setVisibility(View.VISIBLE);

        rg_clean_area.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.tv_clean_area_do_map:
                    map_clean_area.setmOT(MapView.OT.MAP);
                    break;
                case R.id.tv_do_clean_area:
                    map_clean_area.setmOT(MapView.OT.CLEAN_AREA);
                    break;

            }
        });
        rg_clean_area.check(R.id.tv_clean_area_do_map);
    }

    @Override
    public void initData() {
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean result) {
                long selectId = result.getSelectedMapId();
                String cleanAreaData = result.getCleanArea();
                MyLogger.d(TAG, "划区数据1111：" + cleanAreaData);
                IlifeAli.getInstance().getSelectMap(selectId, new OnAliResponse<List<HistoryRecordBean>>() {
                    @Override
                    public void onSuccess(List<HistoryRecordBean> result) {
                        if (result.size() == 0) {//应该只有一条数据
                            return;
                        }
                        MapDataBean mapDataBean = DataUtils.parseSaveMapData(result.get(0).getMapDataArray());
                        if (mapDataBean != null) {
                            map_clean_area.setLeftTopCoordinate(mapDataBean.getLeftX(), mapDataBean.getLeftY());
                            map_clean_area.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
                            map_clean_area.drawMapX8(mapDataBean.getCoordinates());
                        }
                        if (!TextUtils.isEmpty(cleanAreaData)) {
                            JSONObject json = JSON.parseObject(cleanAreaData);
                            times = json.getIntValue("CleanLoop");
                            updateLoopImage();
                            boolean enable = json.getIntValue("Enable")==1;
                            String area = "";
                            if (enable) {
                                area = json.getString("AreaData");
                                if (area.equals("AAAAAAAAAAAAAAAAAAAAAA==")) {
                                    area = "";
                                }
                            }
                            map_clean_area.drawCleanArea(area);
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
        iv_clean_area_time.setImageResource(src);
    }

    @OnClick({R.id.iv_clean_area_time, R.id.fl_top_menu})
    public void onClick(View view) {
        if (view.getId() == R.id.iv_clean_area_time) {
            if (times == 3) {
                times = 0;
            }
            times++;
            updateLoopImage();
        } else {
            String clenAreaData = "{\"CleanAreaData\":{\"AreaData\":\"\",\"CleanLoop\":0,\"Enable\":1}}";
            JSONObject caJson = JSONObject.parseObject(clenAreaData);
            caJson.getJSONObject(EnvConfigure.CleanAreaData).put("AreaData", map_clean_area.getCleanAreaData());
            caJson.getJSONObject(EnvConfigure.CleanAreaData).put("CleanLoop", times);
            MyLogger.d(TAG, "划区数据2222：" + caJson.toString());
            IlifeAli.getInstance().setProperties(caJson, aBoolean -> {
                if (aBoolean) {
                    ToastUtils.showToast("设置划区数据成功");
                }
            });
        }
    }

}
