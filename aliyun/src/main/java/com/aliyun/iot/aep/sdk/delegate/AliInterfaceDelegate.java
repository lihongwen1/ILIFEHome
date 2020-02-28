package com.aliyun.iot.aep.sdk.delegate;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliSetPropertyResponse;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClient;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientFactory;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTCallback;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTResponse;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTUIThreadCallback;
import com.aliyun.iot.aep.sdk.apiclient.emuns.Scheme;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequest;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequestBuilder;
import com.aliyun.iot.aep.sdk.bean.DeviceInfoBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class AliInterfaceDelegate {
    /**
     * list bind device by account
     * whether need a user authentication
     */
    public void listDeviceByAccount(final OnAliResponse<List<DeviceInfoBean>> onAliResponse) {
        Map<String, Object> maps = new HashMap<>();
        IoTRequestBuilder builder = new IoTRequestBuilder()
                .setPath(EnvConfigure.PATH_LIST_BINDING)
                .setScheme(Scheme.HTTPS)
                .setApiVersion(EnvConfigure.API_VER)
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setParams(maps);

        IoTRequest request = builder.build();

        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        ioTAPIClient.send(request, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onFailed(0, e.getMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() != 200) {
                    onAliResponse.onFailed(0, ioTResponse.getMessage());
                } else {
                    Object data = ioTResponse.getData();
                    if (data == null) {
                        onAliResponse.onFailed(0, ioTResponse.getMessage());
                        return;
                    }
                    if (!(data instanceof JSONObject)) {
                        onAliResponse.onFailed(0, ioTResponse.getMessage());
                        return;
                    }
                    Log.d("ListDeviceByAccount","data: "+data);
                    JSONObject jsonObject = (JSONObject) data;
                    JSONArray jsonArray;
                    try {
                        jsonArray = jsonObject.getJSONArray("data");
                        List<DeviceInfoBean> deviceInfoBeanList = JSON.parseArray(jsonArray.toString(), DeviceInfoBean.class);
                        if (deviceInfoBeanList == null ) {
                            deviceInfoBeanList=new ArrayList<>();
                        }
                        onAliResponse.onSuccess(deviceInfoBeanList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }


    /**
     * 解绑设备
     *
     * @param iotId
     * @param ioTCallback
     */
    public static void requestUnbind(String iotId, IoTCallback ioTCallback) {
        Log.d("EqSettingHelp", "_______________" + iotId);
        IoTRequestBuilder builder = new IoTRequestBuilder()
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setScheme(Scheme.HTTPS)
                .setPath(EnvConfigure.PAHT_UNBIND_DEV)
                .setApiVersion(EnvConfigure.API_VER)
                .addParam("iotId", iotId);
        IoTRequest request = builder.build();
        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        ioTAPIClient.send(request, ioTCallback);
    }


    /**
     * 设置时区
     */
    public static void setTimeZone(String iotId) {
        TimeZone timeZone = TimeZone.getDefault();
        String tz = "{\"TimeZone\":{\"TimeZone\":1,\"SummerTime\":2}}";
        com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(tz);
        json.getJSONObject("TimeZone").put("TimeZone", timeZone.getRawOffset() / (3600 * 1000));
        json.getJSONObject("TimeZone").put("SummerTime", timeZone.getDSTSavings());
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, json);
        setPropertyToDevice(params, null);
    }


    /**
     * 获取设备状态错误码（边刷异常，保险杠异常等）
     *
     * @param iotId
     * @param onAliResponse can be null ,null means that this request not need a response
     */
    public static void getCertainDeviceEvent(String iotId, final OnAliResponse<Integer> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_GET_EVENT);
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        IoTRequest ioTRequest = new IoTRequestBuilder()
                .setAuthType("iotAuth")
                .setScheme(Scheme.HTTPS)        // 如果是HTTPS，可以省略本设置
                .setPath(EnvConfigure.PATH_GET_PROPERTIES)                  // 参考业务API文档，设置path
                .setApiVersion(EnvConfigure.API_VER)          // 参考业务API文档，设置apiVersion
                .setParams(params)
                .build();
        ioTAPIClient.send(ioTRequest, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                if (onAliResponse == null) {
                    return;
                }
                if (ioTRequest.getPath().equals(EnvConfigure.PATH_SET_PROPERTIES)) {
                    //Alert the user that the request failed because of the network error
                }
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (onAliResponse == null) {
                    return;
                }
                com.alibaba.fastjson.JSONArray errors = com.alibaba.fastjson.JSONObject.parseArray(ioTResponse.getData().toString());
                if (errors != null && errors.size() > 0) {
                    com.alibaba.fastjson.JSONObject object = (com.alibaba.fastjson.JSONObject) errors.get(0);
                    int errorCode = object.getJSONObject(EnvConfigure.KEY_EVENTBODY).getIntValue(EnvConfigure.KEY_ERRORCODE);
                    if (errorCode != 0) {
                        onAliResponse.onSuccess(errorCode);
                    }
                }
            }
        });
    }

    /**
     * 获取设备属性，结果可用于set status
     *
     * @param iotId
     * @param onAliResponse
     */
    public static void getCertainDevicceProperties(String iotId, final OnAliResponse<String> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_GET_PROPERTY);
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        IoTRequest ioTRequest = new IoTRequestBuilder()
                .setAuthType("iotAuth")
                .setScheme(Scheme.HTTPS)        // 如果是HTTPS，可以省略本设置
                .setPath(EnvConfigure.PATH_GET_PROPERTIES)                  // 参考业务API文档，设置path
                .setApiVersion(EnvConfigure.API_VER)          // 参考业务API文档，设置apiVersion
                .setParams(params)
                .build();
        ioTAPIClient.send(ioTRequest, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                if (ioTRequest.getPath().equals(EnvConfigure.PATH_SET_PROPERTIES)) {
                    //Alert the user that the request failed because of the network error
                }
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() != 200) {
                    //Alert the user that the request failed because of the network error
                } else {
                    //gets the device property,then you can set device status Using 'content'
                    String content = ioTResponse.getData().toString();
                    onAliResponse.onSuccess(content);
                }
            }
        });
    }


    /**
     * 下发属性给设备，example:working mode ,water etc.
     *
     * @param params
     * @param onAliResponse
     */
    public static void setPropertyToDevice(HashMap<String, Object> params, final OnAliSetPropertyResponse onAliResponse) {
        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        IoTRequest ioTRequest = new IoTRequestBuilder()
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setScheme(Scheme.HTTPS)        // 如果是HTTPS，可以省略本设置
                .setPath(EnvConfigure.PATH_SET_PROPERTIES)                  // 参考业务API文档，设置path
                .setApiVersion(EnvConfigure.API_VER)          // 参考业务API文档，设置apiVersion
                .setParams(params)
                .build();
        ioTAPIClient.send(ioTRequest, new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                //Alert the user that the request failed because of the network error
                Log.e("ILIFE_ALI_","设置属性错误:"+e.getLocalizedMessage());
                onAliResponse.onFailed(ioTRequest.getPath(), 0, 0, e.getMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                int responseCode = ioTResponse.getCode();
                if (ioTRequest == null || 200 != responseCode) {
                    onAliResponse.onFailed(ioTRequest.getPath(), 0, 0,ioTResponse.getLocalizedMsg());
                    //Alert the user that the request failed because of the network error
                } else {
                    HashMap<String, Object> params = (HashMap<String, Object>) ioTRequest.getParams();
                    if (params != null && params.containsKey(EnvConfigure.KEY_TAG) && params.containsKey(EnvConfigure.KEY_PATH)) {//containing the  key means that this request was used to  set the device's working mode
                        String path = (String) params.get(EnvConfigure.KEY_PATH);
                        String functioncode = (String) params.get(EnvConfigure.KEY_TAG);
                        if (functioncode != null && path != null) {
                            onAliResponse.onSuccess(path, Integer.parseInt(functioncode), Integer.parseInt(functioncode), ioTResponse.getCode());
                        }
                    }
                }
            }
        }));
    }
}
