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
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
        params.put("limit", 200);
        params.put("order", "desc");
        IoTRequest request = new IoTRequestBuilder()
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setScheme(Scheme.HTTPS)        // 如果是HTTPS，可以省略本设置
                .setPath(EnvConfigure.PATH__GET_PROPERTY_TIME_LINE_LIVE)                  // 参考业务API文档，设置path
                .setApiVersion("1.0.0")          // 参考业务API文档，设置apiVersion
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
                    JSONObject jsonObject = JSON.parseObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray(EnvConfigure.KEY_ITEMS);
                    Gson gson = new Gson();
                    HistoryRecordBean bean;
                    HistoryRecordBean exitBean;
                    int startTime = 0;
                    long timestamp = 0;
                    if (jsonArray == null) {
                        onAliResponse.onFailed(-1, "没有数据");
                        return;
                    }
                    int dataSize = jsonArray.size();
                    if (dataSize > 0) {
                        for (int i = 0; i < jsonArray.size(); i++) {
                            String data = jsonArray.getJSONObject(i).getString(EnvConfigure.KEY_DATA);
                            timestamp = jsonArray.getJSONObject(i).getLong("timestamp");
                            if (data.isEmpty()) {
                                continue;
                            }
                            bean = gson.fromJson(data, HistoryRecordBean.class);
                            startTime = bean.getStartTime();
                            if (String.valueOf(startTime).length() < 10) {
                                continue;
                            }
                            exitBean = mapBeans.get(startTime);
                            Log.e("HISTORY_MAP", "新历史记录---" + generateTime(bean.getStartTime(), "MM月dd日HH:mm:ss") + "---------" + bean.getPackId() + "------------" + bean.getPackNum());
                            if (exitBean == null) {
                                if (IlifeAli.getInstance().getWorkingDevice().getProductKey().equals(EnvConfigure.PRODUCT_KEY_X787)||
                                        IlifeAli.getInstance().getWorkingDevice().getProductKey().equals(EnvConfigure.PRODUCT_KEY_X434)||
                                        IlifeAli.getInstance().getWorkingDevice().getProductKey().equals(EnvConfigure.PRODUCT_KEY_X787)) {
                                    //X787 X434清扫面积需要除去100.
                                    bean.setCleanTotalArea(bean.getCleanTotalArea() / 100);
                                }
                                bean.addCleanData(bean.getPackNum(), bean.getPackId(), bean.getCleanMapData());
                                mapBeans.put(startTime, bean);
                            } else {
                                mapBeans.get(startTime).addCleanData(bean.getPackNum(), bean.getPackId(), bean.getCleanMapData());
                            }
                        }
                    }
                    if (dataSize>=200 && timestamp > start) {
                        end = timestamp;
                        Log.e("HISTORY_MAP", "开始下一次获取下一包历史记录");
                        getHistoryRecords();
                    } else {
                        //the history records have gained,need sorting the cleaning data for every item by package id now
                        List<HistoryRecordBean> beans = new ArrayList<>();
                        for (int i = 0; i < mapBeans.size(); i++) {

                            beans.add(mapBeans.valueAt(i));
                        }
                        onAliResponse.onSuccess(beans);
                    }
                }
            }
        }));
    }

    private String generateTime(long time, String strFormat) {
        SimpleDateFormat format = new SimpleDateFormat(strFormat, Locale.CHINA);
        return format.format(new Date((time) * 1000));
    }








}
