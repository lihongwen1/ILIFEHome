package com.aliyun.iot.aep.sdk.delegate;

import android.app.Application;
import android.text.TextUtils;

import com.aliyun.alink.linksdk.channel.core.base.AError;
import com.aliyun.alink.linksdk.channel.gateway.api.GatewayChannel;
import com.aliyun.alink.linksdk.channel.gateway.api.GatewayConnectConfig;
import com.aliyun.alink.linksdk.channel.gateway.api.GatewayConnectState;
import com.aliyun.alink.linksdk.channel.gateway.api.IGatewayConnectListener;
import com.aliyun.alink.linksdk.channel.mobile.api.IMobileConnectListener;
import com.aliyun.alink.linksdk.channel.mobile.api.IMobileRequestListener;
import com.aliyun.alink.linksdk.channel.mobile.api.MobileChannel;
import com.aliyun.alink.linksdk.channel.mobile.api.MobileConnectConfig;
import com.aliyun.alink.linksdk.channel.mobile.api.MobileConnectState;
import com.aliyun.alink.sdk.jsbridge.BonePluginRegistry;

import com.aliyun.iot.aep.sdk.connectchannel.BoneChannel;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.credential.IotCredentialManager.IoTCredentialListener;
import com.aliyun.iot.aep.sdk.credential.IotCredentialManager.IoTCredentialManageError;
import com.aliyun.iot.aep.sdk.credential.IotCredentialManager.IoTCredentialManageImpl;
import com.aliyun.iot.aep.sdk.credential.data.IoTCredentialData;
import com.aliyun.iot.aep.sdk.framework.sdk.SDKConfigure;
import com.aliyun.iot.aep.sdk.framework.sdk.SimpleSDKDelegateImp;
import com.aliyun.iot.aep.sdk.log.ALog;
import com.aliyun.iot.aep.sdk.login.ILoginStatusChangeListener;
import com.aliyun.iot.aep.sdk.login.LoginBusiness;


import java.util.Map;

/**
 * Created by wuwang on 2017/10/30.
 */

public final class DownstreamConnectorSDKDelegate extends SimpleSDKDelegateImp {

    public final static String ENV_KEY_MQTT_HOST = "ENV_KEY_MQTT_HOST";
    public final static String ENV_KEY_MQTT_CHECK_ROOT_CRT = "ENV_KEY_MQTT_CHECK_ROOT_CRT";
    public final static String ENV_KEY_MQTT_AUTO_HOST = "ENV_KEY_MQTT_AUTO_HOST";
    public final static String ENV_KEY_MQTT_SERVER_AUTO_SELECT_CHANNEL = "ENV_KEY_MQTT_SERVER_AUTO_SELECT_CHANNEL";

    static final private String TAG = "DownstreamConnectorSDKDelegate";

    /* API: ISDKDelegate */

    @Override
    public int init(final Application app, SDKConfigure configure, Map<String, String> args) {
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        for (int i = 0; i < stack.length; i++)
        {
            StackTraceElement s = stack[i];
            System.out.format(" DownstreamConnectorSDKDelegateClassName:%d\t%s\n", i, s.getClassName());
            System.out.format("DownstreamConnectorSDKDelegateMethodName:%d\t%s\n", i, s.getMethodName());
            System.out.format(" DownstreamConnectorSDKDelegate FileName:%d\t%s\n", i, s.getFileName());
            System.out.format("DownstreamConnectorSDKDelegateLineNumber:%d\t%s\n\n", i, s.getLineNumber());
        }
        int ret = 0;
        MobileConnectConfig config = new MobileConnectConfig();

        /* pre start */
        String appKey = args.get(EnvConfigure.KEY_APPKEY);
        boolean autoHost = !"false".equalsIgnoreCase(args.get(ENV_KEY_MQTT_AUTO_HOST));
        String mqttHost = args.get(ENV_KEY_MQTT_HOST);
        boolean isCheckRootCrt = !"false".equalsIgnoreCase(args.get(ENV_KEY_MQTT_CHECK_ROOT_CRT));
        String serverUrlForAutoSelectChannel = args.get(ENV_KEY_MQTT_SERVER_AUTO_SELECT_CHANNEL) == null ? "" : args.get(ENV_KEY_MQTT_SERVER_AUTO_SELECT_CHANNEL);

        //三元组，指定apiclient host
        config.appkey = appKey;
        config.securityGuardAuthcode =EnvConfigure.AUTH_CODE;
        config.autoSelectChannelHost = autoHost;
        config.channelHost = mqttHost;
        config.isCheckChannelRootCrt = isCheckRootCrt;
        config.serverUrlForAutoSelectChannel = serverUrlForAutoSelectChannel;
        MobileChannel.setOpenLog(true);
        // 注意:长连接的初始化只能在主进程中执行，否则会导致互踢问题
        MobileChannel.getInstance().startConnect(app, config, new IMobileConnectListener() {
            @Override
            public void onConnectStateChange(MobileConnectState state) {
                ALog.d(TAG, "onConnectStateChange(), state = " + state.toString());
                EnvConfigure.putEnvArg(EnvConfigure.KEY_TRACE_ID, MobileChannel.getInstance().getClientId());

                //连接成功后，且已登录，则bindaccount
                if (MobileConnectState.CONNECTED.equals(state)) {
                    //连接成功后，且已登录，则bindaccount
                    bindAccount(app);

                    initGateWayChannel(app);
                }
            }
        });

        //2. register logout user
        ((OALoginAdapter) LoginBusiness.getLoginAdapter()).registerBeforeLogoutListener(new OALoginAdapter.OnBeforeLogoutListener() {
            @Override
            public void doAction() {
                MobileChannel.getInstance().unBindAccount(new IMobileRequestListener() {
                    @Override
                    public void onSuccess(String s) {
                        ALog.i(TAG, "mqtt unBindAccount onSuccess ");
                    }

                    @Override
                    public void onFailure(AError aError) {
                        ALog.i(TAG, "mqtt unBindAccount onFailure aError = " + aError.getMsg());
                    }
                });
            }
        });

        LoginBusiness.getLoginAdapter().registerLoginListener(new ILoginStatusChangeListener() {
            @Override
            public void onLoginStatusChange() {
                bindAccount(app);
            }
        });

        //3.register bonebriage
        BonePluginRegistry.register("BoneChannel", BoneChannel.class);

        ALog.d(TAG, "initialized");

        return ret;
    }

    private void bindAccount(Application app) {
        if (!LoginBusiness.isLogin()) {
            return;
        }
        IoTCredentialManageImpl ioTCredentialManage = IoTCredentialManageImpl.getInstance(app);
        if (null == ioTCredentialManage) {
            return;
        }

        if (TextUtils.isEmpty(ioTCredentialManage.getIoTToken())) {
            ioTCredentialManage.asyncRefreshIoTCredential(new IoTCredentialListener() {
                @Override
                public void onRefreshIoTCredentialSuccess(IoTCredentialData ioTCredentialData) {
                    MobileChannel.getInstance().bindAccount(ioTCredentialData.iotToken, new IMobileRequestListener() {
                        @Override
                        public void onSuccess(String s) {
                            ALog.i(TAG, "mqtt bindAccount onSuccess");

                        }

                        @Override
                        public void onFailure(AError aError) {
                            ALog.i(TAG, "mqtt bindAccount onFailure aError = " + aError.getMsg());
                        }
                    });
                }

                @Override
                public void onRefreshIoTCredentialFailed(IoTCredentialManageError ioTCredentialManageError) {
                    ALog.i(TAG, "mqtt bindAccount onFailure ");

                }
            });
        } else {
            MobileChannel.getInstance().bindAccount(ioTCredentialManage.getIoTToken(), new IMobileRequestListener() {
                @Override
                public void onSuccess(String s) {
                    ALog.i(TAG, "mqtt bindAccount onSuccess ");
                }

                @Override
                public void onFailure(AError aError) {
                    ALog.i(TAG, "mqtt bindAccount onFailure aError = " + aError.getMsg());
                }
            });
        }
    }

    void initGateWayChannel(Application app) {
        ALog.i(TAG, "initGateWayChannel app：" + app);
        String clientId = MobileChannel.getInstance().getClientId();
        if (TextUtils.isEmpty(clientId)) {
            //请检查长连接通道是否初始化成功
            com.aliyun.iot.ble.util.Log.w(TAG, "请检查长连接通道是否初始化成功");
            return;
        }

        String mobileProductKey = clientId.split("&")[1];
        String mobileDeviceName = clientId.split("&")[0];

        GatewayConnectConfig config = new GatewayConnectConfig(mobileProductKey, mobileDeviceName, "");
        GatewayChannel.getInstance().startConnect(app, config, new IGatewayConnectListener() {
            @Override
            public void onConnectStateChange(GatewayConnectState state) {
                com.aliyun.iot.ble.util.Log.d(TAG, "onConnectStateChange(), state = " + state.toString());
                if (state == GatewayConnectState.CONNECTED) {
                    com.aliyun.iot.ble.util.Log.d(TAG, "网关建联成功");
                } else if (state == GatewayConnectState.CONNECTFAIL) {
                }
            }
        });
    }
}
