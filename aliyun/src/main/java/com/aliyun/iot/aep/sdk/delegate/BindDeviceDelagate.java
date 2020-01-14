package com.aliyun.iot.aep.sdk.delegate;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.aliyun.alink.business.devicecenter.api.add.AddDeviceBiz;
import com.aliyun.alink.business.devicecenter.api.add.DeviceInfo;
import com.aliyun.alink.business.devicecenter.api.add.IAddDeviceListener;
import com.aliyun.alink.business.devicecenter.api.add.LinkType;
import com.aliyun.alink.business.devicecenter.api.add.ProvisionStatus;
import com.aliyun.alink.business.devicecenter.api.discovery.DiscoveryType;
import com.aliyun.alink.business.devicecenter.api.discovery.IOnDeviceTokenGetListener;
import com.aliyun.alink.business.devicecenter.api.discovery.LocalDeviceMgr;
import com.aliyun.alink.business.devicecenter.base.DCErrorCode;
import com.aliyun.iot.aep.sdk._interface.OnAliBindDeviceResponse;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClient;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientFactory;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTCallback;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTResponse;
import com.aliyun.iot.aep.sdk.apiclient.emuns.Scheme;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequest;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequestBuilder;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.util.WifiUtils;

import org.reactivestreams.Publisher;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.WIFI_SERVICE;

/**
 *
 */
//TODO retry when binding fail
//TODO may need connect to device hotspot when method onProvisioning is calling
public class BindDeviceDelagate {
    private Context context;
    private String homeSsid, homePassword;
    private int TIMEOUT = 90;
    private OnAliBindDeviceResponse<String> onBindDeviceComplete;
    private boolean iscancel = false;
    private Timer timer;
    private int autoUpprogress;
    private int realProgress;
    private String productKey;
    private CompositeDisposable mDisposable;
    private int reBindTimes = 0;
    private String apSsid;

    public BindDeviceDelagate(Context context, String homeSsid, String homePassword, String productKey, OnAliBindDeviceResponse<String> onBindDeviceComplete) {
        this.context = context;
        this.homeSsid = homeSsid;
        this.homePassword = homePassword;
        this.onBindDeviceComplete = onBindDeviceComplete;
        this.mDisposable = new CompositeDisposable();
        if (productKey == null) {
            productKey = "";
        }
        this.productKey = productKey;
    }

    /**
     * 取消配网
     */
    public void cancel() {
        iscancel = true;
        onBindDeviceComplete = null;
        context = null;
        // 停止配网
        LocalDeviceMgr.getInstance().stopDiscovery();
        AddDeviceBiz.getInstance().stopAddDevice();
    }

    public void connectDevice() {

        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
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


    private void discoveryDevice() {
        Log.d("BindDeviceDelagate", "开始发现设备");
        EnumSet<DiscoveryType> discoveryTypeEnumSet = EnumSet.of(DiscoveryType.SOFT_AP_DEVICE);
        LocalDeviceMgr.getInstance().startDiscovery(context, discoveryTypeEnumSet, null, (discoveryType, list) -> {
            Log.d("BindDeviceDelagate", "--发现设备--" + "发现类型---" + discoveryType.getDescription() + "-----设备： " + JSON.toJSONString(list));
            if (list != null && list.size() > 0) {
                boolean isFindDevice = false;
                String targetId = null;
                for (DeviceInfo deviceInfo : list) {
                    if (deviceInfo.productKey.equals(productKey)) {
                        LocalDeviceMgr.getInstance().stopDiscovery();
                        isFindDevice = true;
                        targetId = deviceInfo.id;
                        break;
                    }
                }
                if (isFindDevice && targetId != null && !iscancel) {
                    apSsid = generateAp(targetId);
                    startAddDevice(targetId);
                }
            }
        });
    }


    private void startAddDevice(String id) {
        Log.d("BindDeviceDelagate", "开始添加设备，设备ID为：" + id);
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
                if (dcErrorCode != null) {
                    Log.d("BindDeviceDelagate", "添加设备参数回调" + dcErrorCode.msg);
                } else {
                    Log.d("BindDeviceDelagate", "添加设备参数回调 dcErrorCode为null");
                }
            }

            @Override
            public void onProvisionPrepare(int prepareType) {
                // 手机热点配网、设备热点配网、一键配网、蓝牙辅助配网、二维码配网会走到该流程，
                // 零配和智能路由器配网不会走到该流程。
                // prepareType = 1提示用户输入账号密码
                // prepareType = 2提示用户手动开启指定热点 aha 12345678
                // 执行完上述操作之后，调用toggleProvision接口继续执行配网流程
                if (prepareType == 1) {
                    Log.d("BindDeviceDelagate", "添加设备设置家庭WiFi信息：" + homeSsid + "-----" + homePassword);
                    realProgress = 20;
                    onBindDeviceComplete.onProgress(20);
                    AddDeviceBiz.getInstance().toggleProvision(homeSsid, homePassword, TIMEOUT);
                }
            }

            @Override
            public void onProvisioning() {
                Log.d("BindDeviceDelagate", "配网中。。。。。。。。。。。");
                Disposable disposable = Completable.timer(4, TimeUnit.SECONDS)
                        .subscribe(() -> {
                            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
                            if (!WifiUtils.getSsid(context).equals(apSsid)) {
                                boolean isSuccess = WifiUtils.forceConnectWifi(wifiManager, apSsid, "", 1);
                                Log.d("BindDeviceDelagate", "切换主机wifi，成功:  " + isSuccess);
                            }
                        });
                mDisposable.add(disposable);
                // 配网中
                //may be need to connect to the  device hotspot
            }

            @Override
            public void onProvisionStatus(ProvisionStatus provisionStatus) {
                Log.d("BindDeviceDelagate", "配网状态改变，改变描述信息为： " + provisionStatus.message());

                // 二维码配网会走到这里  provisionStatus=ProvisionStatus.QR_PROVISION_READY表示二维码ready了
                // ProvisionStatus.QR_PROVISION_READY.message() 获取二维码内容
                // 注意：返回二维码时已开启监听设备是否已配网成功的通告，并开始计时，UI端应提示用户尽快扫码；
                // 如果在指定时间配网超时了，重新调用开始配网流程并刷新二维码；
            }

            @Override
            public void onProvisionedResult(boolean b, DeviceInfo deviceInfo, DCErrorCode errorCode) {
                //TODO for promoting the success rate of binding device,you should set a TIME-OUT time for waiting call "onProvisionedResult"
                Log.d("BindDeviceDelagate", "配网结果返回。。。。。。。。。。。");
                // 配网结果 如果配网成功之后包含token，请使用配网成功带的token做绑定
                if (b) {
                    final String mProductKey = deviceInfo.productKey;
                    final String mDeviceName = deviceInfo.deviceName;
                    if (deviceInfo.token != null) {
                        Log.d("BindDeviceDelagate", "使用device info自带的token绑定");
                        bindDevice(mProductKey, mDeviceName, deviceInfo.token);
                    } else {
                        Log.d("BindDeviceDelagate", "使用获取设备token进行绑定");
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
                    }
                } else {
                    //bind fail
                    Log.d("BindDeviceDelagate", "配网失败。。。。。。。。。" + errorCode.toString());
                    bindFail(0, "matching wifi fail");
                }
            }
        });

    }

    public String generateAp(String bssid) {
        StringBuilder sb = new StringBuilder();
        BigInteger id = new BigInteger(bssid.replace(":", ""), 16);
//        long mac = id.longValue() - 1; //sdk1.6.5.2
        long mac = id.longValue();       //sdk1.5.1
        String id_ = Long.toHexString(mac).toUpperCase();
        sb.delete(0, sb.length());
        sb.append("adh_").append(productKey).append("_").append(id_.substring(id_.length() - 6, id_.length()));
        return sb.toString();
    }


    private void bindDevice(String productKey, String deviceName, String token) {
        if (iscancel) {
            return;
        }
        realProgress = 80;
        onBindDeviceComplete.onProgress(80);
        Log.d("BindDeviceDelagate", "开始绑定设备。。。。。");
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

        Single.create((SingleOnSubscribe<String>) emitter ->
                ioTAPIClient.send(ioTRequest, new IoTCallback() {
                    @Override
                    public void onFailure(IoTRequest ioTRequest, Exception e) {
                        /**
                         * 绑定失败后，重置wifi重新绑定
                         */
                        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
                        boolean isSuccess = WifiUtils.forceConnectWifi(wifiManager, homeSsid, homePassword, WifiUtils.getCipherType(homeSsid, wifiManager));
                        Log.d("BindDeviceDelagate", "reBind and re connect WIFI,is toggle wifi succeed:  " + isSuccess);
                        emitter.onError(e);
                    }

                    @Override
                    public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                        Log.d("BindDeviceDelagate", "绑定设备成功。。。。" + ioTResponse.getCode() + "----" + ioTResponse.getMessage() + ioTResponse.getData().toString());
                        if (ioTResponse.getCode() == 200 && ioTResponse.getData() instanceof String) {
                            //bind success
                            String iotId = (String) ioTResponse.getData();
                            emitter.onSuccess(iotId);
                        } else {
                            Log.d("BindDeviceDelagate", "绑定设备失败。。。。。");
                            //bind fail
                            emitter.onError(new Exception(ioTResponse.getMessage() + ioTResponse.getLocalizedMsg()));
                        }
                    }
                })).retryWhen(flowable -> flowable.flatMap((Function<Throwable, Publisher<?>>) throwable -> (Publisher<Boolean>) s -> {
            Disposable disposable = Observable.timer(3, TimeUnit.SECONDS).subscribe(aLong -> {
                if (reBindTimes < 3) {
                    Log.d("BindDeviceDelagate", "延迟3S尝试重新绑定。。。");
                    reBindTimes++;
                    s.onNext(true);
                } else {
                    s.onError(throwable);
                }
            });
            mDisposable.add(disposable);
        })).subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(String s) {
                realProgress = 100;
                bindSuccess(s);
            }

            @Override
            public void onError(Throwable e) {
                //bind fail
                Log.d("BindDeviceDelagate", "绑定设备失败" + e.getMessage());
                bindFail(0, e.getMessage());
            }
        });

    }


    private void bindSuccess(String iotId) {
        if (iscancel) {
            return;
        }
        if (onBindDeviceComplete != null) {
            onBindDeviceComplete.onProgress(100);
            onBindDeviceComplete.onSuccess(iotId);
        }
        if (timer != null) {
            timer.cancel();
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    private void bindFail(int code, String message) {
        if (iscancel) {
            return;
        }
        if (onBindDeviceComplete != null) {
            onBindDeviceComplete.onFailed(code, message);
        }
        if (timer != null) {
            timer.cancel();
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }
}
