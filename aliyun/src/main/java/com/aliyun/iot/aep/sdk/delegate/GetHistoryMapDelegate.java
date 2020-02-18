package com.aliyun.iot.aep.sdk.delegate;

import android.util.Log;

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
import com.aliyun.iot.aep.sdk.bean.RealTimeMapBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 获取实时地图的历史地图代理
 */
public class GetHistoryMapDelegate {
    private static final String TAG = "GetHistoryMapDelegate";
    private IoTAPIClient ioTAPIClient;
    private String iotId;
    private long start;
    private long end;
    List<RealTimeMapBean> beanList = new ArrayList<>();
    OnAliResponse<List<RealTimeMapBean>> onAliResponse;

    public GetHistoryMapDelegate(IoTAPIClient ioTAPIClient, String iotId, long start, long end, OnAliResponse<List<RealTimeMapBean>> onAliResponse) {
        this.ioTAPIClient = ioTAPIClient;
        this.iotId = iotId;
        this.start = start;
        this.end = end;
        this.onAliResponse = onAliResponse;
    }

    public void getRealHistoryMap() {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put("identifier", EnvConfigure.KEY_REALTIMEMAP);
        params.put("start", start);
        Log.d(TAG,"历史数据结束时间："+end);
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
        ioTAPIClient.send(request,new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.e(TAG, "getCleaningHistory onFailure e = " + e.toString());
                onAliResponse.onFailed(0, e.getMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                try {
                    if (ioTResponse.getCode() == 200) {
                        Log.d(TAG, "getCleaningHistory-----DATA:     " + ioTResponse.getData().toString());
                        String content = ioTResponse.getData().toString();
                        JSONObject jsonObject = JSON.parseObject(content);
                        JSONArray jsonArray = jsonObject.getJSONArray(EnvConfigure.KEY_ITEMS);
                        String data;
                        RealTimeMapBean bean;
                        for (int i = 0; i < jsonArray.size(); i++) {
                            data = jsonArray.getJSONObject(i).getString(EnvConfigure.KEY_DATA);
                            Gson gson = new Gson();
                            bean = gson.fromJson(data, RealTimeMapBean.class);
                            beanList.add(bean);
                        }
                        long timeStamp = jsonArray.getJSONObject(jsonArray.size() - 1).getLong("timestamp");
                        Log.d(TAG, "SLAM TIME: " + timeStamp + " ----:" + start + "-----:" + jsonArray.size());
                        if (jsonArray.size() == 200 && timeStamp > start) {//地图数据未完全获取，需进一步获取
                            Log.d(TAG,"进一步获取数据..........");
                            end = timeStamp;
                            getRealHistoryMap();
                        } else {
                            Collections.reverse(beanList);
                            onAliResponse.onSuccess(beanList);
                        }
                    }
                } catch (Exception e) {

                }
            }
        });
    }
}
