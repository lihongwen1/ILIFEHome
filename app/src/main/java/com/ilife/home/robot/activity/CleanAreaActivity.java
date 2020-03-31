package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.view.MapView;

import java.util.List;

import butterknife.BindView;

public class CleanAreaActivity extends BackBaseActivity {
    private static final String TAG = "CleanAreaActivity";
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.map_clean_area)
    MapView map_clean_area;
    private String cleanAreaData;
    private int times;//清扫次数
    private int enable;//0-无效  1-开始 2-进行中

    @Override
    public int getLayoutId() {
        return R.layout.activity_clean_area;
    }

    @Override
    public void initView() {
        tv_title.setText("划区清扫");
        map_clean_area.setmOT(MapView.OT.MAP);
    }

    @Override
    public void initData() {
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean result) {
                long selectId = result.getSelectedMapId();
                cleanAreaData = result.getPartition();
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
                        if (TextUtils.isEmpty(cleanAreaData)) {
                            map_clean_area.drawCleanArea(cleanAreaData);
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
}
