package com.aliyun.iot.aep.sdk.contant;

import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sdk.android.openaccount.OpenAccountSDK;
import com.alibaba.sdk.android.openaccount.callback.LoginCallback;
import com.alibaba.sdk.android.openaccount.model.OpenAccountSession;
import com.alibaba.sdk.android.openaccount.ui.OpenAccountUIService;
import com.alibaba.sdk.android.openaccount.ui.util.ToastUtils;
import com.aliyun.alink.linksdk.channel.core.base.AError;
import com.aliyun.alink.linksdk.channel.mobile.api.IMobileConnectListener;
import com.aliyun.alink.linksdk.channel.mobile.api.IMobileDownstreamListener;
import com.aliyun.alink.linksdk.channel.mobile.api.IMobileSubscrbieListener;
import com.aliyun.alink.linksdk.channel.mobile.api.MobileChannel;
import com.aliyun.iot.aep.sdk.IoTSmart;
import com.aliyun.iot.aep.sdk._interface.OnAliBindDeviceResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliResponseSingle;
import com.aliyun.iot.aep.sdk._interface.OnAliSetPropertyResponse;
import com.aliyun.iot.aep.sdk._interface.OnDevicePoropertyResponse;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClient;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientFactory;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTCallback;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTResponse;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTUIThreadCallback;
import com.aliyun.iot.aep.sdk.apiclient.emuns.Scheme;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequest;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequestBuilder;
import com.aliyun.iot.aep.sdk.bean.DeviceInfoBean;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.OTAInfoBean;
import com.aliyun.iot.aep.sdk.bean.OTAUpgradeBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.bean.RealTimeMapBean;
import com.aliyun.iot.aep.sdk.bean.ScheduleBean;
import com.aliyun.iot.aep.sdk.credential.IotCredentialManager.IoTCredentialManageImpl;
import com.aliyun.iot.aep.sdk.delegate.AliInterfaceDelegate;
import com.aliyun.iot.aep.sdk.delegate.BindDeviceDelagate;
import com.aliyun.iot.aep.sdk.delegate.GetHistoryMapDelegate;
import com.aliyun.iot.aep.sdk.delegate.GetHistoryRecordDelegate;
import com.aliyun.iot.aep.sdk.framework.AApplication;
import com.aliyun.iot.aep.sdk.framework.sdk.SDKManager;
import com.aliyun.iot.aep.sdk.framework.utils.SpUtil;
import com.aliyun.iot.aep.sdk.helper.SDKInitHelper;
import com.aliyun.iot.aep.sdk.login.ILoginCallback;
import com.aliyun.iot.aep.sdk.login.ILogoutCallback;
import com.aliyun.iot.aep.sdk.login.LoginBusiness;
import com.aliyun.iot.aep.sdk.login.data.UserInfo;
import com.aliyun.iot.aep.sdk.threadpool.ThreadPool;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * //TODO WORKINGDEVICE 需要序列化到本地
 */
public class IlifeAli {
    /**
     * 用户所有的设备
     */
    private List<DeviceInfoBean> mAcUserDevices;
    private static final String TAG = "ILIFE_ALI_";
    private static IlifeAli instance;
    /**
     * 可能为空，需要序列化到本地，并且在为空的时候重新反序列化回来
     */
    private DeviceInfoBean workingDevice;
    private AApplication aApplication;
    private IoTAPIClient ioTAPIClient;
    private String iotId;
    private IMobileDownstreamListener downListener;
    private IMobileSubscrbieListener topicListener;//订阅topic
    private IMobileConnectListener mConnectListener;
    private String bindingProductKey;//绑定中设备的product key;
    private int CONNECTION_STATUS = -1;

    public static synchronized IlifeAli getInstance() {
        if (instance == null) {
            synchronized (IlifeAli.class) {
                if (instance == null) {
                    Log.e(TAG, "构建单例对象--------");
                    instance = new IlifeAli();
                }
            }
        }
        return instance;
    }


    private IoTRequest buildRequest(String path, HashMap<String, Object> params) {
        return new IoTRequestBuilder()
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setScheme(Scheme.HTTPS)        // 如果是HTTPS，可以省略本设置
                .setPath(path)                  // 参考业务API文档，设置path
                .setApiVersion(EnvConfigure.API_VER)          // 参考业务API文档，设置apiVersion
                .setParams(params)
                .build();
    }

    public List<DeviceInfoBean> getmAcUserDevices() {
        if (mAcUserDevices == null) {
            mAcUserDevices = new ArrayList<>();
        }
        return mAcUserDevices;
    }

    public void setmAcUserDevices(List<DeviceInfoBean> devices) {
        if (mAcUserDevices == null) {
            mAcUserDevices = new ArrayList<>();
        }
        mAcUserDevices.clear();
        mAcUserDevices.addAll(devices);
    }

    /**
     * 解注册，重置变量 etc
     */
    public void reset() {
        workingDevice = null;
        if (topicListener != null) {
            MobileChannel.getInstance().unSubscrbie(EnvConfigure.TOPIC, topicListener);
            topicListener = null;
        }
        if (downListener != null) {
            MobileChannel.getInstance().unRegisterDownstreamListener(downListener);
            downListener = null;
        }
    }


    public void setWorkingDevice(DeviceInfoBean workingDevice) {
        this.workingDevice = workingDevice;
        this.iotId = workingDevice.getIotId();
        Gson gson = new Gson();
        Log.d(TAG,"机器状态数据："+gson.toJson(workingDevice));
        SpUtil.putString(aApplication, "key_working_device", gson.toJson(workingDevice,DeviceInfoBean.class));
    }

    public DeviceInfoBean getWorkingDevice() {
        if (workingDevice == null) {
            Gson gson = new Gson();
            workingDevice = gson.fromJson(SpUtil.getString(aApplication, "key_working_device"), DeviceInfoBean.class);
            if (workingDevice == null) {
                workingDevice = new DeviceInfoBean();
            } else {
                this.iotId = workingDevice.getIotId();
            }
        }
        return workingDevice;
    }


    public String getIotId() {
        return iotId;
    }

    public void setIotId(String iotId) {
        this.iotId = iotId;
    }


    /**
     * 设置账号登录异常监听器
     *
     * @param tTokenInvalidListener
     */
    public void settTokenInvalidListener(OnAliResponseSingle<Boolean> tTokenInvalidListener) {
        IoTCredentialManageImpl.getInstance(aApplication).setIotTokenInvalidListener(() -> {
            //会话失效
            if (tTokenInvalidListener != null) {
                tTokenInvalidListener.onResponse(true);
            }
        });
    }


    /**
     * 初始化账号，通道等
     *
     * @param context
     */
    public void init(AApplication context) {
        this.aApplication = context;
        ioTAPIClient = new IoTAPIClientFactory().getClient();
        registerConnectionChangeListener();
    }

    /**
     * 应用销毁时调用
     */
    public void destroy() {
        reset();
        unRegisterConnectionLister();
    }


    /**
     * 解注册长连接状态改变监听，注册是在APP初始化时
     */
    public void unRegisterConnectionLister() {
        if (mConnectListener != null) {
            MobileChannel.getInstance().unRegisterConnectListener(mConnectListener);
        }
    }


    public void logOut(final OnAliResponse<String> onAliResponse) {
        LoginBusiness.logout(new ILogoutCallback() {
            @Override
            public void onLogoutSuccess() {
                onAliResponse.onSuccess("login success");
            }

            @Override
            public void onLogoutFailed(int i, String s) {
                onAliResponse.onFailed(i, s);
            }
        });
    }


    /**
     * 判断账号是否登录
     *
     * @return
     */
    public boolean isLogin() {
        boolean isLogin = LoginBusiness.isLogin();
        Log.d(TAG, "是否已登录。。。" + isLogin);
        return isLogin;
    }

    /**
     * 登录/判断已登录
     *
     * @param onAliResponse
     */
    public void login(final OnAliResponse<Boolean> onAliResponse) {
        LoginBusiness.login(new ILoginCallback() {
            @Override
            public void onLoginSuccess() {
                onAliResponse.onSuccess(true);
            }

            @Override
            public void onLoginFailed(int code, String error) {
                onAliResponse.onFailed(code, error);
            }
        });

    }


    /**
     * @param onAliResponse
     */
    public void selectACountry(OnAliResponse<String> onAliResponse) {
        Log.d(TAG, "开始选择国家");
        IoTSmart.getCountryList(new IoTSmart.ICountryListGetCallBack() {
            @Override
            public void onSucess(List<IoTSmart.Country> list) {
                IoTSmart.Country selectCountry = null;
                for (IoTSmart.Country country : list) {
                    if (country.areaName.equals("新加坡")) {
                        selectCountry = country;
                        break;
                    }
                }
                if (selectCountry != null) {
                    final String selectCountryName = selectCountry.areaName;
                    IoTSmart.setCountry(selectCountry, needRestartApp -> {
                        if (needRestartApp) {
                            onAliResponse.onFailed(-1, "set country success,and need restart app");
                        } else {//重新初始化
                            SDKInitHelper.init(AApplication.getInstance(), "");
                            onAliResponse.onSuccess(selectCountryName);
                        }
                    });
                }
            }

            @Override
            public void onFail(String s, int i, String s1) {
                Log.d(TAG, "get country list failed ,the reason is:" + s + "---" + s1);
            }
        });
    }


    /**
     * 绑定设备
     *
     * @param homeSsid
     * @param homePassword
     * @param onBindDeviceComplete
     */
    public BindDeviceDelagate bindDevice(String homeSsid, String homePassword, OnAliBindDeviceResponse<String> onBindDeviceComplete) {
        BindDeviceDelagate bindDeviceDelagate = new BindDeviceDelagate(aApplication, homeSsid, homePassword, bindingProductKey, onBindDeviceComplete);
        bindDeviceDelagate.connectDevice();
        return bindDeviceDelagate;
    }


    public void reNameDevice(String name, final OnAliResponseSingle<Boolean> onAliResponseSingle) {
        if (iotId == null || iotId.isEmpty()) {
            onAliResponseSingle.onResponse(false);
            return;
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_DEV_NICKNAME, name);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_SET_DEV_NICK_NAME, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponseSingle.onResponse(false);
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() != 200) {
                    onAliResponseSingle.onResponse(false);
                } else {
                    onAliResponseSingle.onResponse(true);
                }
            }
        }));
    }

    /**
     * 获取账号下的设备列表
     *
     * @param onAliResponse
     */
    public void listDeviceByAccount(OnAliResponse<List<DeviceInfoBean>> onAliResponse) {
        AliInterfaceDelegate aliInterfaceDelegate = new AliInterfaceDelegate();
        aliInterfaceDelegate.listDeviceByAccount(onAliResponse);
    }

    /**
     * 解绑设备
     *
     * @param iotId
     * @param onAliResponse
     */
    public void unBindDevice(final String iotId, final OnAliResponse<Boolean> onAliResponse) {
        AliInterfaceDelegate aliInterfaceDelegate = new AliInterfaceDelegate();
        aliInterfaceDelegate.requestUnbind(iotId, new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                //unbind fail
                onAliResponse.onFailed(0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                //unbind success
                if (ioTResponse.getCode() != 200) {
                    onAliResponse.onFailed(0, ioTResponse.getLocalizedMsg());
                } else {
                    onAliResponse.onSuccess(true);
                }
            }
        }));
    }


    /**
     * 订阅TOPIC,订阅topic，若失败会检查长连接，长连接成功后，会再次绑定
     */
    public void registerSubscribeTopic() {
        if (topicListener == null) {
            topicListener = new IMobileSubscrbieListener() {
                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "registerSubscribeTopic success: " + s);
                }

                @Override
                public void onFailed(String s, AError aError) {
                    checkAndReconnection();
                    Log.d(TAG, "registerSubscribeTopic failed: " + s + "----" + aError.getDomain());
                }

                @Override
                public boolean needUISafety() {
                    return false;
                }
            };
        }
        MobileChannel.getInstance().subscrbie(EnvConfigure.TOPIC, topicListener);
    }

    public void registerDownStream(final OnDevicePoropertyResponse onDevicePoropertyResponse) {
        if (downListener == null) {
            downListener = new IMobileDownstreamListener() {
                @Override
                public void onCommand(String method, String data) {
                    Log.d(TAG, "method:      " + method + "---      data:" + data);
                    JSONObject object = JSONObject.parseObject(data);
                    String iot = object.getString(EnvConfigure.KEY_IOT_ID);
                    if (!iot.equals(iotId)) {//
                        return;
                    }
                    switch (method) {
                        case EnvConfigure.METHOD_THING_PROP:
                            JSONObject items = object.getJSONObject(EnvConfigure.KEY_ITEMS);
                            if (items != null) {
                                if (items.containsKey(EnvConfigure.KEY_POWER_SWITCH)) {
                                    ToastUtils.toast(aApplication, items.getJSONObject(EnvConfigure.KEY_POWER_SWITCH).getString(EnvConfigure.KEY_VALUE));
                                } else if (items.containsKey(EnvConfigure.KEY_WORK_MODE)) {//工作状态
                                    onDevicePoropertyResponse.onStatusChange(items.getJSONObject(EnvConfigure.KEY_WORK_MODE).getIntValue(EnvConfigure.KEY_VALUE));
                                } else if (items.containsKey(EnvConfigure.KEY_REALTIMEMAP)) {//实时地图数据
                                    Log.e(TAG, "时间数据：" + items.getJSONObject(EnvConfigure.KEY_REALTIMEMAP).getLong(EnvConfigure.KEY_TIME));
                                    onDevicePoropertyResponse.onRealMap(items.getJSONObject(EnvConfigure.KEY_REALTIMEMAP).getString(EnvConfigure.KEY_VALUE));
                                } else if (items.containsKey(EnvConfigure.KEY_REAL_TIME_MAP_START)) {
                                    long marStartTime = items.getJSONObject(EnvConfigure.KEY_REAL_TIME_MAP_START).getLongValue(EnvConfigure.KEY_TIME);
                                    onDevicePoropertyResponse.onRealTimeMapStart(marStartTime);
                                } else if (items.containsKey(EnvConfigure.KEY_BATTERY_STATE)) {
                                    int battery = items.getJSONObject(EnvConfigure.KEY_BATTERY_STATE).getIntValue(EnvConfigure.KEY_VALUE);
                                    onDevicePoropertyResponse.onBatterState(battery);
                                }
                            }
                            break;
                        case EnvConfigure.METHOD_THING_EVENT:
                            int errorCode = object.getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_ERRORCODE);
                            onDevicePoropertyResponse.onError(errorCode);
                            break;
                    }
                }

                @Override
                public boolean shouldHandle(String method) {
                    // method 即为Topic,e.g. /thing/properties,/thing/events,/thing/status，如果该Topic需要处理，返回true后onCommand才会回调。
                    Log.d(TAG, "method:      " + method);
                    return method.equals(EnvConfigure.METHOD_THING_PROP) || method.equals(EnvConfigure.METHOD_THING_EVENT);
                }
            };
        }
        registerSubscribeTopic();
        MobileChannel.getInstance().registerDownstreamListener(true, downListener);
    }


    public void unRegisterConnectionChangeListener() {
        if (mConnectListener != null) {
            MobileChannel.getInstance().unRegisterConnectListener(mConnectListener);
        }
    }


    /**
     * 注册长连接状态变化监听
     */
    public void registerConnectionChangeListener() {
        /** 注册通道的状态变化,记得调用 unRegisterConnectListener */
        if (mConnectListener == null) {
            mConnectListener = state -> {
                int stateCode = -1;
                String value = "";
                //参考 MobileConnectState.CONNECTED
                switch (state) {
                    case CONNECTED:
                        //已连接
                        value = "连接改变：已连接";
                        stateCode = 1;
                        if (topicListener != null) {//若topicListener为null，则页面为画图页面，需要重新订阅topic
                            registerSubscribeTopic();
                        }
                        break;
                    case DISCONNECTED:
                        value = "连接改变，已断开";
                        //已断开
                        stateCode = 2;
                        break;
                    case CONNECTING:
                        value = "连接改变，连接中";
                        //连接中
                        stateCode = 3;
                        break;
                    case CONNECTFAIL:
                        //TODO 长连接失败后，标记为未初始化，等待重新连接
                        value = "连接改变，连接失败，重新标记为未初始化";
                        //连接失败
                        stateCode = 4;
                        break;
                }
                CONNECTION_STATUS = stateCode;
                Log.d(TAG, value);
//                Toast.makeText(aApplication,value,Toast.LENGTH_LONG).show();
            };
        }
        MobileChannel.getInstance().registerConnectListener(true, mConnectListener);
    }

    /**
     * 长连接如果失败，会导致APP无法接收到服务器下发。
     * 检查长连接状态，如果未连接，则尝试重连
     * aliyun level 8 方法已经不可用
     */
    public void checkAndReconnection() {
//        if (CONNECTION_STATUS == -1) {
//            return;
//        }
//        if (CONNECTION_STATUS == 2 || CONNECTION_STATUS == 4) {
//            Log.e(TAG, "云服务器长连接已断开，正尝试重新连接。。。。。。。。");
//            SDKManager.prepareForInitSdk(aApplication);
//        } else {
//            Log.d(TAG, "设备连接状态正常。。。。。。。。。。。。");
//        }
    }

    public void setProperties(HashMap<String, Object> params, OnAliSetPropertyResponse onAliResponse) {
        AliInterfaceDelegate.setPropertyToDevice(params, onAliResponse);
    }

    public void getProperties(final OnAliResponse<PropertyBean> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_GET_PROPERTY);
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_GET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onFailed(0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() == 200) {
                    Log.d(TAG, "get propertied data:    " + ioTResponse.getData().toString());
                    JSONObject jsonObject = JSON.parseObject(ioTResponse.getData().toString());
                    boolean max = jsonObject.getJSONObject(EnvConfigure.KEY_MAX_MODE).getIntValue(EnvConfigure.KEY_VALUE) == 1;
                    int battery = jsonObject.getJSONObject(EnvConfigure.KEY_BATTERY_STATE).getIntValue(EnvConfigure.KEY_VALUE);
                    int workMode = jsonObject.getJSONObject(EnvConfigure.KEY_WORK_MODE).getIntValue(EnvConfigure.KEY_VALUE);
                    int waterLevel = jsonObject.getJSONObject(EnvConfigure.KEY_WATER_CONTROL).getIntValue(EnvConfigure.KEY_VALUE);
                    long startTimeLine;
                    if (jsonObject.containsKey(EnvConfigure.KEY_REAL_TIME_MAP_START)) {
                        startTimeLine = jsonObject.getJSONObject(EnvConfigure.KEY_REAL_TIME_MAP_START).getLongValue(EnvConfigure.KEY_TIME);
                    } else {
                        startTimeLine = System.currentTimeMillis();
                    }
//                    long historyTimeLine = jsonObject.getJSONObject(EnvConfigure.KEY_HISTORY_START_TIME).getIntValue(EnvConfigure.KEY_VALUE);
                    long historyTimeLine = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000;//获取一周的清扫记录
                    boolean voiceOpen = true;
                    if (jsonObject.containsKey(EnvConfigure.KEY_BEEP_NO_DISTURB)) {
                        voiceOpen = jsonObject.getJSONObject(EnvConfigure.KEY_BEEP_NO_DISTURB).getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_SWITCH) == 0;
                    } else {
                        Log.d(TAG, "数据无语音开关字段");
                    }
                    onAliResponse.onSuccess(new PropertyBean(max, workMode, battery, waterLevel, startTimeLine, historyTimeLine, voiceOpen));
                } else {
                    onAliResponse.onFailed(0, ioTResponse.getLocalizedMsg());
                }
            }
        }));
    }

    public UserInfo getUserInfo() {
        return LoginBusiness.getUserInfo();
    }

    public void queryUserData(final OnAliResponse<String> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        HashMap<String, Object> ids = new HashMap<>();
        List<String> list = new ArrayList<>();
        list.add(IoTCredentialManageImpl.getInstance(aApplication).getIoTCredential().identity);
        ids.put("identityIds", list);
        params.put("request", ids);
        IoTRequest request = new IoTRequestBuilder()
                .setScheme(Scheme.HTTPS)        // 如果是HTTPS，可以省略本设置
                .setPath(EnvConfigure.PATH_QUERY_ACCOUNT)                  // 参考业务API文档，设置path
                .setApiVersion("1.0.4")          // 参考业务API文档，设置apiVersion
                .setParams(params)
                .build();
        ioTAPIClient.send(request, new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onFailed(0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                Log.e(TAG, "user account data:" + ioTResponse.getData().toString());
                if (ioTResponse.getCode() != 200) {
                    onAliResponse.onFailed(0, ioTResponse.getLocalizedMsg());
                } else {
                    onAliResponse.onSuccess(ioTResponse.getData().toString());

                }
            }
        }));
    }

    /**
     * {
     * "id":1508232047194,
     * "request": {
     * "iotToken": "109049c80bcde4c06b15f6f62e29a3ba",
     * "apiVer": "1.0.5"
     * },
     * "params": {
     * "request": {"identityId":"50e5opda16ebf5558e000a660ac9632a038c2479", "accountMetaV2":{"phone":"15757286621", "appKey":"60039075","nickName":"activity_register_phone"}}
     * },
     * "version": "1.0"
     * }
     *
     * @param onAliResponse
     */
    public void resetNickName(String nickName, final OnAliResponseSingle<Boolean> onAliResponse) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("displayName", nickName);
        OpenAccountUIService oas = OpenAccountSDK.getService(OpenAccountUIService.class);
        oas.updateProfile(aApplication, map, new LoginCallback() {
            @Override
            public void onSuccess(OpenAccountSession openAccountSession) {
                onAliResponse.onResponse(true);
            }

            @Override
            public void onFailure(int i, String s) {
                onAliResponse.onResponse(false);
            }
        });
    }

    public void qrBindDevice(String shareCode, final OnAliResponseSingle<Boolean> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_QR_KEY, shareCode);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_BIND_BY_SHARECODE, params), new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onResponse(false);
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                onAliResponse.onResponse(ioTResponse.getCode() == 200);
            }
        });

    }

    /**
     * 非管理员不能分享
     *
     * @param iotId
     * @param onAliResponse
     */
    public void showShareQrCode(final String iotId, final OnAliResponse<String> onAliResponse) {
        ArrayList<String> iotIdList = new ArrayList<>();
        iotIdList.add(iotId);
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID_LIST, iotIdList);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_GENE_SHARE_CODE, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onFailed(0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() != 200) {
                    onAliResponse.onFailed(0, ioTResponse.getLocalizedMsg());
                } else {
                    onAliResponse.onSuccess(ioTResponse.getData().toString());
                }

            }
        }));

    }

    /**
     * 请求示例： "params": {
     * "appVersion": "10.0.0",
     * "type": 1,
     * "productKey": "a1OwEjYFJNb",
     * "content": "重试多次配网失败",
     * "mobileModel": "iPhoneX",
     * "contact": "15066666666",
     * "mobileSystem": "ios",
     * "appVersion": "1.1",
     * "iotId": "fy2Z1oZFWZQVii6kkFVM00101edf00",
     * "topic": "设备无法配网",
     * "devicename": "手环"
     * }
     *
     * @param contact
     * @param content
     * @param appVersion
     * @param iotId
     * @param onAliResponse
     * @param type          问题类型
     * @param robotName     机器类型
     */
    public void commitFeedback(String contact, String content, int type, String robotName, String appVersion, String iotId, String productKey, final OnAliResponseSingle<Boolean> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("productKey", productKey);
        params.put("content", content);
        params.put("mobileModel", Build.MODEL);
        params.put("contact", contact);
        params.put("mobileSystem", "Android");
        params.put("appVersion", appVersion);
        params.put("iotId", iotId);
        params.put("topic", "");
        params.put("devicename", robotName);
        Log.d(TAG, "上传数据：" + params.toString());
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_ADD_FEEDBACK, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onResponse(false);
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() == 200) {
                    onAliResponse.onResponse(true);
                } else {
                    Log.e(TAG, "发送反馈数据失败：" + ioTResponse.getLocalizedMsg());
                    onAliResponse.onResponse(false);
                }
            }
        }));
    }


    public void setTimeZone() {
        String tz = "{\"TimeZone\":{\"TimeZone\":1,\"SummerTime\":2}}";
        TimeZone timeZone = TimeZone.getDefault();
        HashMap<String, Object> params = new HashMap<>();
        JSONObject json = JSONObject.parseObject(tz);
        json.getJSONObject("TimeZone").put("TimeZone", timeZone.getRawOffset() / (3600 * 1000));
        Date date = new GregorianCalendar().getTime();
        json.getJSONObject("TimeZone").put("SummerTime", timeZone.inDaylightTime(date) ? 1 : 0);
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, json);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_SET_PROPERTIES, params), new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.d(TAG, "设置时钟失败。。。。。。。。。。。。");
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                Log.d(TAG, "设置时钟成功。。。。。。。。。。。。" + json.toString());
            }
        });
    }

    public void getErrorEvent(final OnAliResponseSingle<Integer> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_GET_EVENT);
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_GET_EVENT, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.d(TAG, "获取设备事件----错误码失败！");
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() == 200) {
                    String data = ioTResponse.getData().toString();
                    Log.d(TAG, "获取设备事件成功：  " + data);
                    JSONArray errors = JSONObject.parseArray(data);
                    if (errors != null && errors.size() > 0) {
                        JSONObject object = (JSONObject) errors.get(0);
                        if (object.containsKey(EnvConfigure.KEY_EVENTBODY)) {
                            int errorCode = object.getJSONObject(EnvConfigure.KEY_EVENTBODY).getIntValue(EnvConfigure.KEY_ERRORCODE);
                            if (errorCode != 0) {
                                onAliResponse.onResponse(errorCode);
                            }
                        }
                    }
                }
            }
        }));
    }

    public void findDevice(final OnAliSetPropertyResponse onResponse) {
        JSONObject json_find = JSONObject.parseObject("{\"FindRobot\":1}");
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, json_find);
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_FIND_ROBOT);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_SET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onResponse.onFailed(ioTRequest.getPath(), EnvConfigure.VALUE_FIND_ROBOT, 0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                onResponse.onSuccess(ioTRequest.getPath(), EnvConfigure.VALUE_FIND_ROBOT, 1, ioTResponse.getCode());
            }
        }));

    }

    public void resetDeviceToFactory(final OnAliSetPropertyResponse onResponse) {
        JSONObject json = JSONObject.parseObject("{\"ResetFactory\":1}");
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, json);
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_FAC_RESET);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_SET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onResponse.onFailed(ioTRequest.getPath(), EnvConfigure.VALUE_FAC_RESET, 0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                onResponse.onSuccess(EnvConfigure.PATH_SET_PROPERTIES, EnvConfigure.VALUE_FAC_RESET, 1, ioTResponse.getCode());
//                cloudResetFactory(onResponse);
            }
        }));
    }

    private void cloudResetFactory(final OnAliSetPropertyResponse onResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_RESET_FACTORY, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onResponse.onFailed(ioTRequest.getPath(), EnvConfigure.VALUE_FAC_RESET, 0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                onResponse.onSuccess(EnvConfigure.PATH_SET_PROPERTIES, EnvConfigure.VALUE_FAC_RESET, 1, ioTResponse.getCode());
            }
        }));
    }

    public void waterControl(final int level, final OnAliSetPropertyResponse onResponse) {
        String jsonStr = "";
        switch (level) {
            case 0:
                jsonStr = "{\"WaterTankContrl\":0}";
                break;
            case 1:
                jsonStr = "{\"WaterTankContrl\":1}";
                break;
            case 2:
                jsonStr = "{\"WaterTankContrl\":2}";
                break;
            case 3:
                jsonStr = "{\"WaterTankContrl\":3}";
                break;
        }
        JSONObject json = JSONObject.parseObject(jsonStr);
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, json);
        params.put(EnvConfigure.KEY_EXTRA, level);
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_SET_WATER);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_SET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onResponse.onFailed(ioTRequest.getPath(), EnvConfigure.VALUE_SET_WATER, 0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                onResponse.onSuccess(EnvConfigure.PATH_SET_PROPERTIES, EnvConfigure.VALUE_SET_WATER, level, ioTResponse.getCode());
            }
        }));
    }

    public void setMaxMode(int maxMode, final OnAliSetPropertyResponse onResponse) {
        String jsonStr;
        final int mode;
        if (maxMode == 0) {
            mode = 1;
            jsonStr = "{\"MaxMode\":1}";
        } else {
            mode = 0;
            jsonStr = "{\"MaxMode\":0}";
        }
        JSONObject json = JSONObject.parseObject(jsonStr);
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, json);
        params.put(EnvConfigure.KEY_EXTRA, mode);
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_SET_MAX);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_SET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onResponse.onFailed(ioTRequest.getPath(), EnvConfigure.VALUE_SET_MAX, 0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                onResponse.onSuccess(EnvConfigure.PATH_SET_PROPERTIES, EnvConfigure.VALUE_SET_MAX, mode, ioTResponse.getCode());
            }
        }));
    }


    public void setVoiceOpen(boolean isOpen, final OnAliSetPropertyResponse onResponse) {
        final int isDisturbOpen = isOpen ? 0 : 1;
        String jst = "{\"BeepNoDisturb\":{\"Switch\":0,\"Time\":0}}";
        JSONObject json = JSONObject.parseObject(jst);
        json.getJSONObject(EnvConfigure.KEY_BEEP_NO_DISTURB).put(EnvConfigure.KEY_SWITCH, isDisturbOpen);
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, json);
        params.put(EnvConfigure.KEY_EXTRA, isDisturbOpen);
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_SET_VOICE_SWITCH);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_SET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onResponse.onFailed(ioTRequest.getPath(), EnvConfigure.VALUE_SET_VOICE_SWITCH, 0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() == 200) {
                    onResponse.onSuccess(EnvConfigure.PATH_SET_PROPERTIES, EnvConfigure.VALUE_SET_VOICE_SWITCH, isDisturbOpen, ioTResponse.getCode());
                } else {
                    Log.d(TAG, "请求失败，错误信息： " + ioTResponse.getLocalizedMsg());
                }
            }
        }));
    }


    public void setSchedule(boolean isNewScheduleVersion, int position, final int open, final int hour, final int minute, final OnAliResponse<ScheduleBean> onResponse) {
        String schedule;
        if (isNewScheduleVersion) {
            schedule = "{\"Schedule\":{\"ScheduleHour\":0,\"ScheduleType\":0,\"ScheduleEnd\":300,\"ScheduleEnable\":0,\"ScheduleMode\":6,\"ScheduleWeek\":1,\"ScheduleArea\":\"AAAAAAAAAAAAAAAA\",\"ScheduleMinutes\":0}}";
        } else {
            schedule = "{\"Schedule\":{\"ScheduleHour\":0,\"ScheduleEnd\":300,\"ScheduleEnable\":0,\"ScheduleMode\":3,\"ScheduleWeek\":1,\"ScheduleArea\":1,\"ScheduleMinutes\":0}}";
        }
        position = position == 0 ? 7 : position;
        String str = EnvConfigure.KEY_SCHEDULE + position;
        String schedule_ = schedule.replaceFirst(EnvConfigure.KEY_SCHEDULE, str);
        final JSONObject json = JSONObject.parseObject(schedule_);
        json.getJSONObject(str).put(EnvConfigure.KEY_SCHEDULE_ENABLE, open);
        json.getJSONObject(str).put(EnvConfigure.KEY_SCHEDULE_HOUR, hour);
        json.getJSONObject(str).put(EnvConfigure.KEY_SCHEDULE_MINUTES, minute);
        json.getJSONObject(str).put(EnvConfigure.KEY_SCHEDULE_WEEK, position);
        HashMap<String, Object> params = new HashMap();
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, json);
        params.put(EnvConfigure.KEY_POSITION, position);
        Log.d(TAG, "VALUE:   " + json);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_SET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onResponse.onFailed(0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() == 200) {
                    ScheduleBean scheduleBean = new ScheduleBean();
                    scheduleBean.setScheduleEnable(open);
                    scheduleBean.setScheduleMinutes(minute);
                    scheduleBean.setScheduleHour(hour);
                    onResponse.onSuccess(scheduleBean);
                } else {
                    Log.e(TAG, "预约错误：" + ioTResponse.getLocalizedMsg());
                }
            }
        }));
    }

    public void getScheduleInfo(final OnAliResponse<String> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_GET_PROPERTY);
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_GET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onFailed(0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() == 200) {
                    String content = ioTResponse.getData().toString();
                    onAliResponse.onSuccess(content);
                }
            }
        }));
    }

    public void queryConsumer(final OnAliResponse<String> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_GET_PROPERTY);
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_GET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onFailed(0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() == 200) {
                    String data = ioTResponse.getData().toString();
                    if (data.contains(EnvConfigure.KEY_PARTS_STATUS)) {
                        onAliResponse.onSuccess(data);
                    } else {
                        Log.e(TAG, "获取耗材数据不完整");
                        onAliResponse.onFailed(0, "数据不完整");
                    }
                }
            }
        }));
    }

    public void resetConsumer(int sideBrushLife, int rollLife, int fillterLife, final OnAliResponse<String> onAliResponse) {
        String str = "{\"PartsStatus\":{\"FilterLife\":0,\"SideBrushLife\":0,\"MainBrushLife\":0}}";
        JSONObject json = JSONObject.parseObject(str);
        json.getJSONObject(EnvConfigure.KEY_PARTS_STATUS).put(EnvConfigure.KEY_SIDE_BRUSH_LIFE, sideBrushLife);
        json.getJSONObject(EnvConfigure.KEY_PARTS_STATUS).put(EnvConfigure.KEY_MAIN_BRUSH_LIFE, rollLife);
        json.getJSONObject(EnvConfigure.KEY_PARTS_STATUS).put(EnvConfigure.KEY_FILTER_LIFE, fillterLife);
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, json);
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_SET_PARTSTIME);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_SET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onFailed(0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() == 200) {
                    onAliResponse.onSuccess(ioTResponse.getData().toString());
                }
            }
        }));
    }

    /**
     * 实时地图页面的历史清扫数据
     *
     * @param start
     * @param end
     * @param onAliResponse
     */
    //地图数据多于200包的处理 TODO pkgid乱序
    public void getCleaningHistory(long start, long end, OnAliResponse<List<RealTimeMapBean>> onAliResponse) {
        GetHistoryMapDelegate history = new GetHistoryMapDelegate(ioTAPIClient, iotId, start, end, onAliResponse);
        history.getRealHistoryMap();
    }


    /**
     * 清扫记录数据
     *
     * @param end
     */
    public void getHistoryRecords(long start, long end, final OnAliResponse<List<HistoryRecordBean>> onAliResponse) {
        GetHistoryRecordDelegate delegate = new GetHistoryRecordDelegate(start, end, onAliResponse, ioTAPIClient, iotId);
        delegate.getHistoryRecords();
    }


    /**
     * 云端查询固件升级安装包版本
     *
     * @param onAliResponse
     */
    public void queryOTAInstallPkg(final OnAliResponse<OTAInfoBean> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_QUERY_OTA_VER, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onFailed(0, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (ioTResponse.getCode() != 200 || ioTResponse.getData() == null) {
                    onAliResponse.onFailed(0, "response error,and error message is" + ioTResponse.getLocalizedMsg());
                } else {
                    String content = ioTResponse.getData().toString();
                    Log.d(TAG, "获取设备OTA信息：" + ioTResponse.getData().toString());
                    OTAInfoBean otaInfoBean = new OTAInfoBean();
                    JSONObject jsonObject = JSON.parseObject(ioTResponse.getData().toString());
                    if (jsonObject != null) {
                        String tarVer = jsonObject.getString("version");//mcu-00.99.99.09-app-1.1.0.05-20191123.084932
                        String[] values = tarVer.split("-");
                        int tVer = Integer.valueOf(values[1].replace(".", ""));
                        otaInfoBean.setTargetVer(tVer);
                        String curVer = jsonObject.getString("currentVersion");//mcu-00.20.03.09-app-1.1.0.05-20191123.084932
                        String[] cValues = curVer.split("-");
                        int cVer = Integer.valueOf(cValues[1].replace(".", ""));
                        otaInfoBean.setCurrentVer(cVer);
                        otaInfoBean.setWholeTargetVer(tarVer);
                        onAliResponse.onSuccess(otaInfoBean);
                    } else {
                        onAliResponse.onFailed(0, "未获取到OTA版本");
                    }
                }
            }
        }));
    }

    public void ensureDownloadOTA(OnAliResponse<Boolean> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        List<String> iotIds = new ArrayList<>();
        iotIds.add(iotId);
        params.put(EnvConfigure.KEY_IOT_IDS, iotIds);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_OTA_UPGRADE, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.d(TAG, "确认设备升级失败---" + e.getLocalizedMessage());
                onAliResponse.onFailed(-1, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                Log.d(TAG, "确认设备升级---" + ioTResponse.getData().toString());
                if (ioTResponse.getCode() == 200) {
                    onAliResponse.onSuccess(true);
                } else {
                    onAliResponse.onFailed(-1, ioTResponse.getLocalizedMsg());
                }
            }
        }));

    }

    public void queryDownloadProgress(String ver, OnAliResponse<OTAUpgradeBean> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_VERSION, ver);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_OTA_QUERY_UPGRADE_PROGRESS, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.d(TAG, "获取设备固件升级进度信息失败---" + e.getLocalizedMessage());
                onAliResponse.onFailed(-1, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                Log.d(TAG, "获取设备固件升级进度信息---" + ioTResponse.getData().toString());
                if (ioTResponse.getCode() == 200 && ioTResponse.getData() != null) {
                    Gson gson = new Gson();
                    OTAUpgradeBean upgradeBean = gson.fromJson(ioTResponse.getData().toString(), OTAUpgradeBean.class);
                    onAliResponse.onSuccess(upgradeBean);
                } else {
                    onAliResponse.onFailed(-1, ioTResponse.getLocalizedMsg());
                }
            }
        }));
    }


    /**
     * 查询主机是否有可升级的固件版本
     *
     * @param onAliResponse
     */
    public void queryRobotOtaVer(final OnAliResponse<OTAInfoBean> onAliResponse) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_GET_PROPERTY);
        params.put("identifier", EnvConfigure.KEY_OTA_INFO);
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_GET_PROPERTIES, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                onAliResponse.onFailed(0, e.getMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                String content = ioTResponse.getData().toString();
                if (ioTResponse.getCode() != 200 || content == null) {
                    onAliResponse.onFailed(0, "response error,and error message is" + ioTResponse.getMessage());
                } else {
                    Log.d(TAG, "获取设备OTA信息：" + ioTResponse.getData().toString());
                    JSONObject json = JSONObject.parseObject(content);
                    if (json.containsKey(EnvConfigure.KEY_OTA_INFO)) {
                        String data = json.getJSONObject(EnvConfigure.KEY_OTA_INFO).getString(EnvConfigure.KEY_VALUE);
                        OTAInfoBean otaInfoBean = new Gson().fromJson(data, OTAInfoBean.class);
                        if (otaInfoBean == null) {
                            onAliResponse.onFailed(0, "response error,and error message is" + ioTResponse.getMessage());
                        } else {
                            if (otaInfoBean.getUpdateProgess() == 100 && otaInfoBean.getUpdateState() == 0) {//进度100，状态为0,代表更新成功
                                otaInfoBean.setUpdateState(4);
                            }
                            onAliResponse.onSuccess(otaInfoBean);
                        }
                    } else {
                        onAliResponse.onFailed(0, "response error,and error message is 未发现OTA信息");
                    }
                }
            }
        }));
    }


    public void reportInstallPkgVer(String version) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_VERSION, version);
        ioTAPIClient.send(buildRequest(EnvConfigure.PATH_REPORY_OTA_VER, params), new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.d(TAG, "上班版本号失败---" + e.getLocalizedMessage());
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                Log.d(TAG, "上班版本号成功---");
            }
        }));
    }


    /**
     * 获取支持添加的设备列表
     */
    public void getSupportDeviceListFromSever() {
        Map<String, Object> maps = new HashMap<>();
        IoTRequestBuilder builder = new IoTRequestBuilder()
                .setPath("/thing/productInfo/getByAppKey")
                .setApiVersion("1.1.3")
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setParams(maps);

        IoTRequest request = builder.build();

        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        ioTAPIClient.send(request, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.d(TAG, "0----");
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                final int code = ioTResponse.getCode();
                final String msg = ioTResponse.getMessage();
                Log.d(TAG, "DATA:  " + ioTResponse.getData().toString());

            }
        });
    }


    public void taobaoAuthorization(String authCode, OnAliResponse<String> onAliResponse) {
        JSONObject params = new JSONObject();
        Map<String, Object> requestMap = params.getInnerMap();
        params.put("authCode", authCode);
        IoTRequest ioTRequest = new IoTRequestBuilder()
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setApiVersion("1.0.5")
                .setPath("/account/taobao/bind")
                .setParams(requestMap)
                .setScheme(Scheme.HTTPS)
                .build();
        new IoTAPIClientFactory().getClient().send(ioTRequest, new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.e("TaobaoAuthActivity", "授权淘宝账号失败--------------" + e.getMessage());
                onAliResponse.onFailed(0, "授权失败");
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                Log.d("TaobaoAuthActivity", "授权淘宝账号成功-----------");
                switch (ioTResponse.getCode()) {
                    case 200:
                        onAliResponse.onSuccess("授权成功");
                        break;
                    case 400:
                        onAliResponse.onFailed(400, "账号已被绑定");
                        break;


                }

            }
        }));
    }

    public void checkTaobaoAuthorization(OnAliResponseSingle<Boolean> onaliResponse) {
        JSONObject params = new JSONObject();
        Map<String, Object> requestMap = params.getInnerMap();
        params.put("accountType", "TAOBAO");
        IoTRequest ioTRequest = new IoTRequestBuilder()
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setApiVersion("1.0.5")
                .setPath("/account/taobao/bind")
                .setParams(requestMap)
                .setScheme(Scheme.HTTPS)
                .build();
        new IoTAPIClientFactory().getClient().send(ioTRequest, new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.e("TaobaoAuthActivity", "授权淘宝账号失败--------------" + e.getMessage());
                onaliResponse.onResponse(false);
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                Log.d("TaobaoAuthActivity", "授权淘宝账号成功-----------");
                onaliResponse.onResponse(true);
            }
        }));
    }

    /**
     * 解除淘宝账号授权
     *
     * @param onAliResponse
     */
    public void unAuthorizationTaobao(OnAliResponseSingle<Boolean> onAliResponse) {
        JSONObject params = new JSONObject();
        Map<String, Object> requestMap = params.getInnerMap();
        params.put("accountType", "TAOBAO");
        IoTRequest ioTRequest = new IoTRequestBuilder()
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setApiVersion("1.0.5")
                .setPath("/account/thirdparty/unbind")
                .setParams(requestMap)
                .setScheme(Scheme.HTTPS)
                .build();

        new IoTAPIClientFactory().getClient().send(ioTRequest, new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.e("TaobaoAuthActivity", "取消授权淘宝账号失败--------------" + e.getMessage());
                onAliResponse.onResponse(false);
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                Log.d("TaobaoAuthActivity", "取消授权淘宝账号成功-----------");
                onAliResponse.onResponse(true);
            }
        }));
    }

    /**
     * 获取淘宝账号授权专题
     *
     * @param onAliResponse
     */
    public void getAuthorizationTaobao(OnAliResponseSingle<Boolean> onAliResponse) {
        JSONObject params = new JSONObject();
        Map<String, Object> requestMap = params.getInnerMap();
        params.put("accountType", "TAOBAO");
        IoTRequest ioTRequest = new IoTRequestBuilder()
                .setAuthType(EnvConfigure.IOT_AUTH)
                .setApiVersion("1.0.5")
                .setPath("/account/thirdparty/get")
                .setParams(requestMap)
                .setScheme(Scheme.HTTPS)
                .build();

        new IoTAPIClientFactory().getClient().send(ioTRequest, new IoTUIThreadCallback(new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                Log.e("TaobaoAuthActivity", "获取授权淘宝账号失败--------------" + e.getMessage());
                onAliResponse.onResponse(false);
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                Log.d("TaobaoAuthActivity", "获取授权淘宝账号成功-----------" + ioTResponse.getData());
                if (ioTResponse.getCode() == 200 && ioTResponse.getData() != null) {
                    JSONObject jsonObject = JSON.parseObject(ioTResponse.getData().toString());
                    if (jsonObject != null && jsonObject.containsKey("accountId")) {
                        onAliResponse.onResponse(true);
                    } else {
                        onAliResponse.onResponse(false);
                    }
                } else {
                    onAliResponse.onResponse(false);
                }
            }
        }));
    }

    public String getBindingProductKey() {
        return bindingProductKey == null ? "" : bindingProductKey;
    }

    public void setBindingProductKey(String bindingProductKey) {
        this.bindingProductKey = bindingProductKey;
    }

}
