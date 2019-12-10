package com.aliyun.iot.aep.sdk.delegate;

import android.util.Log;
import android.util.SparseArray;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClient;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTCallback;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTResponse;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTUIThreadCallback;
import com.aliyun.iot.aep.sdk.apiclient.emuns.Scheme;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequest;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequestBuilder;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetHistoryRecordDelegate {
    private long start;
    private long end;
    private OnAliResponse<List<HistoryRecordBean>> onAliResponse;
    private IoTAPIClient ioTAPIClient;
    private String iotId;
    private SparseArray<HistoryRecordBean> mapBeans = new SparseArray<>();

    public GetHistoryRecordDelegate(long start, long end, OnAliResponse<List<HistoryRecordBean>> onAliResponse, IoTAPIClient ioTAPIClient, String iotId) {
        this.start = start;
        this.end = end;
        this.onAliResponse = onAliResponse;
        this.ioTAPIClient = ioTAPIClient;
        this.iotId = iotId;
    }

    /**
     * 清扫记录数据
     */
    public void getHistoryRecords() {
        HashMap<String, Object> params = new HashMap<>();
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put("identifier", EnvConfigure.KEY_CLEAN_HISTORY);
        params.put("start", start);
        params.put("end", end);
        params.put("pageSize", 200);
        params.put("ordered", false);
        IoTRequest request = new IoTRequestBuilder()
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setScheme(Scheme.HTTPS)        // 如果是HTTPS，可以省略本设置
                .setPath(EnvConfigure.PATH_GET_PROPERTY_TIMELINE)                  // 参考业务API文档，设置path
                .setApiVersion(EnvConfigure.API_VER)          // 参考业务API文档，设置apiVersion
                .setParams(params)
                .build();
        ioTAPIClient.send(request, new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() == 200) {
                    String result = ioTResponse.getData().toString();
                    Log.d("HISTORY_MAP","历史地图数据："+result);
                    ArrayList<ArrayList<String>> dataList = new ArrayList<>();
                    JSONObject jsonObject = JSON.parseObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray(EnvConfigure.KEY_ITEMS);
                    Gson gson = new Gson();
                    HistoryRecordBean bean;
                    HistoryRecordBean exitBean;
                    int startTime = 0;
                    if (jsonArray==null){
                        onAliResponse.onFailed(0,"没有数据");
                        return;
                    }
                    int dataSize = jsonArray.size();
                    if (dataSize > 0) {
                        for (int i = 0; i < jsonArray.size(); i++) {
                            String data = jsonArray.getJSONObject(i).getString(EnvConfigure.KEY_DATA);
                            if (data.isEmpty()) {
                                continue;
                            }
                            bean = gson.fromJson(data, HistoryRecordBean.class);
                            startTime = bean.getStartTime();
                            if (String.valueOf(startTime).length() < 10) {
                                continue;
                            }
                            Log.i("HISTORY_MAP_Index",mapBeans.indexOfKey(startTime)+"");
                            Log.e("HISTORY_MAP", generateTime(bean.getStartTime(),"MM月dd日HH:mm:ss") + "---------" + bean.getPackId() + "------------" + bean.getPackNum());

                            exitBean=mapBeans.get(startTime);
                            if (exitBean==null) {
                                bean.addCleanData(bean.getCleanMapData());
                                mapBeans.put(startTime, bean);
                            } else if (!exitBean.getMapDataList().contains(bean.getCleanMapData())){
                                mapBeans.get(startTime).addCleanData(bean.getCleanMapData());
                            }else {
                                Log.d("HISTORY_MAP","存在pkgid相同的数据包。。。");
                            }
                        }
                    }
                    if (dataSize > 200 && startTime > start) {
                        end = startTime;
                        Log.e("HISTORY_MAP","开始下一次获取下一包历史记录");
                        getHistoryRecords();
                    } else {
                        List<HistoryRecordBean> beans = new ArrayList<>();
                        for (int i = 0; i <mapBeans.size(); i++) {
                            beans.add(mapBeans.valueAt(i));
                        }
                        onAliResponse.onSuccess(beans);
                    }
                }
            }
        }));
    }

    public String generateTime(long time, String strFormat) {
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        String str = format.format(new Date((time) * 1000));
        return str;
    }
}
