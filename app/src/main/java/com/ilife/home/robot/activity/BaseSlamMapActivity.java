package com.ilife.home.robot.activity;

import android.text.TextUtils;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.bean.SaveMapBean;
import com.ilife.home.robot.bean.SaveMapDataInfoBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;

/**
 * 底图Activity基类
 */
public abstract class BaseSlamMapActivity extends BackBaseActivity {
    public static final String KEY_SAVE_MAP_DATA = "save_map_data";
    protected SaveMapBean saveMapBean;
    protected PropertyBean propertyBean;

    @Override
    public void initData() {
        super.initData();
        saveMapBean = new SaveMapBean();
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean bean) {
                propertyBean = bean;
                long mapId = bean.getSelectedMapId();
                if (mapId == 0) {
                    onDataReady();
                    return;
                }
                saveMapBean.setMapId((int) mapId);
                String saveMapDataKey = "";
                if (mapId == bean.getSaveMapDataMapId1()) {
                    saveMapDataKey = EnvConfigure.KEY_SAVE_MAP_DATA_1;
                } else if (mapId == bean.getSaveMapDataMapId2()) {
                    saveMapDataKey = EnvConfigure.KEY_SAVE_MAP_DATA_2;
                } else if (mapId == bean.getSaveMapDataMapId3()) {
                    saveMapDataKey = EnvConfigure.KEY_SAVE_MAP_DATA_3;
                }
                if (TextUtils.isEmpty(saveMapDataKey)) {
                    onDataReady();
                    return;
                }
                IlifeAli.getInstance().getSaveMapData(mapId, saveMapDataKey, new OnAliResponse<String[]>() {
                    @Override
                    public void onSuccess(String[] result) {
                        saveMapBean.setMapData(result);
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
                        onDataReady();
                    }
                });
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
                    saveMapBean.setMapDataInfo(result);
                    onDataReady();
                }

                @Override
                public void onFailed(int code, String message) {
                    onDataReady();
                }
            });
        }
    }
   private void onDataReady(){
        if (!isDestroyed()){
            onSaveMapBean();
        }
   }
    protected abstract void onSaveMapBean();
}
