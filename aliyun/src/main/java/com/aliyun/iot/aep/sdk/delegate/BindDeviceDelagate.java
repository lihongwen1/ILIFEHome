package com.aliyun.iot.aep.sdk.delegate;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.nfc.tech.NfcA;
import android.os.CountDownTimer;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.aliyun.alink.business.devicecenter.api.add.AddDeviceBiz;
import com.aliyun.alink.business.devicecenter.api.add.DeviceInfo;
import com.aliyun.alink.business.devicecenter.api.add.IAddDeviceListener;
import com.aliyun.alink.business.devicecenter.api.add.LinkType;
import com.aliyun.alink.business.devicecenter.api.add.ProvisionStatus;
import com.aliyun.alink.business.devicecenter.api.discovery.DiscoveryType;
import com.aliyun.alink.business.devicecenter.api.discovery.IDeviceDiscoveryListener;
import com.aliyun.alink.business.devicecenter.api.discovery.IOnDeviceTokenGetListener;
import com.aliyun.alink.business.devicecenter.api.discovery.LocalDeviceMgr;
import com.aliyun.alink.business.devicecenter.base.DCErrorCode;
import com.aliyun.iot.aep.sdk._interface.OnAliBindDeviceResponse;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClient;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientFactory;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTCallback;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTResponse;
import com.aliyun.iot.aep.sdk.apiclient.emuns.Env;
import com.aliyun.iot.aep.sdk.apiclient.emuns.Scheme;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequest;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequestBuilder;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.log.ALog;
import com.aliyun.iot.aep.sdk.util.WifiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.WIFI_SERVICE;

/**
 *
 */
//TODO you should query product information before  binding device.
//TODO may need connect to device hotspot when method onProvisioning is calling
public class BindDeviceDelagate {
    private Context context;
    private String homeSsid, homePassword;
    private int TIMEOUT = 60;
    private OnAliBindDeviceResponse<String> onBindDeviceComplete;
    private boolean iscancel;
    private Timer timer;
    private int autoUpprogress;
    private int realProgress;
    private int timerTimes;
    private String productKey;
    private List<DeviceInfo> localFindDevice;
    private int BINDIND_STEP = 0;//0-未开始 1-finding 2-binding 3-fail 4-success
    public BindDeviceDelagate(Context context, String homeSsid, String homePassword, String productKey, OnAliBindDeviceResponse<String> onBindDeviceComplete) {
        this.context = context;
        this.homeSsid = homeSsid;
        this.homePassword = homePassword;
        this.onBindDeviceComplete = onBindDeviceComplete;
        this.localFindDevice = new ArrayList<>();
        if (productKey == null) {
            productKey = "";
        }
        this.productKey = productKey;
    }

    public void cancel() {
        iscancel = true;
        onBindDeviceComplete = null;
        context = null;
    }

    public void connectDevice() {

        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timerTimes++;
                    if (onBindDeviceComplete != null) {
                        autoUpprogress += 3;
                        if (autoUpprogress > 90) {
                            autoUpprogress = 90;
                        }
                        if (autoUpprogress > realProgress) {
                            realProgress = autoUpprogress;
                        }
                        onBindDeviceComplete.onProgress(realProgress);
                    }

                }
            }, 0, 1000);
        }
        discoveryDevice();
    }

//    private void discoveryDevice() {
//        Log.d("Bind_DEBICE", "开始发现设备");
//        EnumSet<DiscoveryType> discoveryTypeEnumSet = EnumSet.allOf(DiscoveryType.class);
//        LocalDeviceMgr.getInstance().startDiscovery(context, discoveryTypeEnumSet, null, new IDeviceDiscoveryListener() {
//            @Override
//            public void onDeviceFound(DiscoveryType discoveryType, List<DeviceInfo> list) {
//                Log.d("Bind_DEBICE", "discoveryType:  " + discoveryType.getDescription());
//                Log.d("Bind_DEBICE", "--发现设备--" + JSON.toJSONString(list));
//                final List<DeviceInfo> foundDevice = new ArrayList<>();
//                if (list != null && list.size() > 0) {
//                    for (DeviceInfo deviceInfo : list) {
//                        if (deviceInfo.productKey.equals(productKey)) {
//                            foundDevice.add(deviceInfo);
//                        }
//                    }
//                } else {
//                    Log.d("Bind_DEBICE", "未发现设备");
//                    bindFail(0, "not found any device by call onDeviceFound");
//                }
////                filterDevice(foundDevice);
//            }
//
//        });
//    }


    private void discoveryDevice() {
        Log.d("Bind_DEBICE", "开始发现设备");
        EnumSet<DiscoveryType> discoveryTypeEnumSet = EnumSet.allOf(DiscoveryType.class);
        LocalDeviceMgr.getInstance().startDiscovery(context, discoveryTypeEnumSet, null, new IDeviceDiscoveryListener() {
            @Override
            public void onDeviceFound(DiscoveryType discoveryType, List<DeviceInfo> list) {
                Log.d("Bind_DEBICE", "--发现设备--" + JSON.toJSONString(list));
                if (list != null && list.size() > 0) {
                    boolean isFindDevice = false;
                    String targetId = null;
                    for (DeviceInfo deviceInfo : list) {
                        if (deviceInfo.productKey.equals(productKey) && deviceInfo.deviceName == null) {
                            LocalDeviceMgr.getInstance().stopDiscovery();
                            isFindDevice = true;
                            targetId = deviceInfo.id;
                            break;
                        }
                    }
                    if (isFindDevice && targetId != null) {
                        startAddDevice(targetId);
                    }
                }
            }

        });
    }


    /**
     * 服务器过滤设备
     *
     * @param foundDevice 已经通过product key过滤的设备
     */
    private void filterDevice(List<DeviceInfo> foundDevice) {
        Log.d("Bind_DEBICE", "开始过滤设备。。。。");
        List<Map<String, String>> devices = new ArrayList<>();
        for (DeviceInfo deviceItem : foundDevice) {
            Map<String, String> device = new HashMap<>(2);
            device.put("productKey", deviceItem.productKey);
            device.put("deviceName", deviceItem.deviceName);
            devices.add(device);
        }
        IoTRequest request = new IoTRequestBuilder()
                .setPath(EnvConfigure.PATH_BIND_SERVER_FILTER)
                .setApiVersion(EnvConfigure.API_VER)
                .addParam("iotDevices", devices)
                .setAuthType(EnvConfigure.IOT_AUTH)
                .build();

        new IoTAPIClientFactory().getClient().send(request, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.d("Bind_DEBICE", "过滤设备失败");
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (200 != ioTResponse.getCode()) {
                    return;
                }

                if (!(ioTResponse.getData() instanceof JSONArray)) {
                    return;
                }

                JSONArray items = (JSONArray) ioTResponse.getData();
                //有返回数据，表示服务端支持此pk，dn
                if (null != items) {
                    Log.d("Bind_DEBICE", "有返回数据，表示服务端支持此pk，dn" + ioTResponse.getData());
                    List<String> names = parseFilterDevice(items);//过滤后可以配网绑定的设备
                    for (DeviceInfo info : foundDevice) {
                        for (String name : names) {
                            if (name.contains(info.id)) {
                                localFindDevice.add(info);
                            }
                        }
                    }
                }
                //过滤设备完成，发现设备即可开始绑定
                if (localFindDevice.size() > 0) {
                    LocalDeviceMgr.getInstance().stopDiscovery();
                    startAddDevice(localFindDevice.get(0).id);
                }
            }
        });
    }


    private List<String> parseFilterDevice(JSONArray jsonArray) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                names.add(jsonObject.getString("deviceName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return names;
    }


    private void startAddDevice(String id) {
        Log.d("Bind_DEBICE", "开始添加设备，设备ID为：" + id);
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.productKey = productKey;
        deviceInfo.id = id;
        deviceInfo.linkType = LinkType.ALI_SOFT_AP.getName();
        AddDeviceBiz.getInstance().setDevice(deviceInfo);
        AddDeviceBiz.getInstance().setAliProvisionMode(LinkType.ALI_SOFT_AP.getName());
        // 开始添加设备
        AddDeviceBiz.getInstance().startAddDevice(context, new IAddDeviceListener() {
            @Override
            public void onPreCheck(boolean b, DCErrorCode dcErrorCode) {
                // 参数检测回调
                Log.d("Bind_DEBICE", "添加设备参数回调");
            }

            @Override
            public void onProvisionPrepare(int prepareType) {
                Log.d("Bind_DEBICE", "添加设备设置家庭WiFi信息：" + homeSsid + "-----" + homePassword);
                // 手机热点配网、设备热点配网、一键配网、蓝牙辅助配网、二维码配网会走到该流程，
                // 零配和智能路由器配网不会走到该流程。
                // prepareType = 1提示用户输入账号密码
                // prepareType = 2提示用户手动开启指定热点 aha 12345678
                // 执行完上述操作之后，调用toggleProvision接口继续执行配网流程
                realProgress = 20;
                onBindDeviceComplete.onProgress(20);
                AddDeviceBiz.getInstance().toggleProvision(homeSsid, homePassword, TIMEOUT);
            }

            @Override
            public void onProvisioning() {
                Log.d("Bind_DEBICE", "配网中。。。。。。。。。。。开始连接家庭网络");
                // 配网中
                //may be need to connect to the  device hotspot
            }

            @Override
            public void onProvisionStatus(ProvisionStatus provisionStatus) {
                // 二维码配网会走到这里  provisionStatus=ProvisionStatus.QR_PROVISION_READY表示二维码ready了
                // ProvisionStatus.QR_PROVISION_READY.message() 获取二维码内容
                // 注意：返回二维码时已开启监听设备是否已配网成功的通告，并开始计时，UI端应提示用户尽快扫码；
                // 如果在指定时间配网超时了，重新调用开始配网流程并刷新二维码；
            }

            @Override
            public void onProvisionedResult(boolean b, DeviceInfo deviceInfo, DCErrorCode errorCode) {
                Log.d("Bind_DEBICE", "配网结果返回。。。。。。。。。。。");
                // 配网结果 如果配网成功之后包含token，请使用配网成功带的token做绑定
                if (b && deviceInfo != null) {
                    final String mProductKey = deviceInfo.productKey;
                    final String mDeviceName = deviceInfo.deviceName;
                    LocalDeviceMgr.getInstance().getDeviceToken(context, deviceInfo.productKey, deviceInfo.deviceName, 60 * 1000, 5 * 1000, new IOnDeviceTokenGetListener() {
                        @Override
                        public void onSuccess(String token) {
                            bindDevice(mProductKey, mDeviceName, token);
                        }

                        @Override
                        public void onFail(String reason) {
                            bindFail(0, "matching wifi succeed,but getting device token  fail" + reason);
                        }
                    });
                } else {
                    //bind fail
                    Log.d("Bind_DEBICE", "配网失败。。。。。。。。。" + errorCode.toString());
                    bindFail(0, "matching wifi fail");
                }
            }
        });

    }


    private void bindDevice(String productKey, String deviceName, String token) {
        realProgress = 80;
        onBindDeviceComplete.onProgress(80);
        Log.d("Bind_DEBICE", "开始绑定设备。。。。。");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("productKey", productKey);
        paramMap.put("deviceName", deviceName);
        paramMap.put("token", token);
        IoTRequest ioTRequest = new IoTRequestBuilder()
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setScheme(Scheme.HTTPS)
                .setPath(EnvConfigure.PATH_BIND_DEVICE)
                .setApiVersion(EnvConfigure.API_VER)
                .setParams(paramMap)
                .build();
        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        ioTAPIClient.send(ioTRequest, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                //bind fail
                Log.d("Bind_DEBICE", "绑定设备失败" + e.getMessage());
                bindFail(0, e.getMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                Log.d("Bind_DEBICE", "绑定设备成功。。。。" + ioTResponse.getCode() + "----" + ioTResponse.getMessage() + ioTResponse.getData().toString());
                if (ioTResponse.getCode() == 200 && ioTResponse.getData() instanceof String) {
                    //bind success
                    final String iotId = (String) ioTResponse.getData();
                    timer.cancel();
                    realProgress = 100;
                    onBindDeviceComplete.onProgress(100);
                    onBindDeviceComplete.onSuccess(iotId);
                } else {
                    Log.d("Bind_DEBICE", "绑定设备失败。。。。。");
                    //bind fail
                    bindFail(ioTResponse.getCode(), ioTResponse.getMessage() + ioTResponse.getLocalizedMsg());
                }
            }
        });
    }

    private void bindFail(int code, String message) {
        if (onBindDeviceComplete != null) {
            onBindDeviceComplete.onFailed(code, message);
        }
        if (timer != null) {
            timer.cancel();
        }
    }

}
