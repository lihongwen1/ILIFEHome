package com.aliyun.iot.aep.sdk.helper;

import android.app.Application;

import com.aliyun.alink.linksdk.tools.ThreadTools;
import com.aliyun.iot.aep.sdk.delegate.APIGatewaySDKDelegate;
import com.aliyun.iot.aep.sdk.delegate.OpenAccountSDKDelegate;
import com.aliyun.iot.aep.sdk.framework.AApplication;
import com.aliyun.iot.aep.sdk.framework.config.AConfigure;
import com.aliyun.iot.aep.sdk.framework.sdk.SDKConfigure;
import com.aliyun.iot.aep.sdk.framework.sdk.SDKManager;
import com.aliyun.iot.aep.sdk.log.ALog;

/**
 * Created by wuwang on 2017/10/30.
 */
public class ApplicationHelper {

    public void onCreate(AApplication application) {
        String packageName = application.getPackageName();

        // SDK 仅在主进程初始化, 多进程初始化可能出现问题，例如 长连接 SDK
        if (!packageName.equals(ThreadTools.getProcessName(application, android.os.Process.myPid()))) {
            return;
        }
        initALog(application);
        this.init(application);
    }

    /* methods: helper init */
    private void init(AApplication application) {

        /* 初始化 SDK */
        this._initBaseSdk(application);

        this._initOtherSdk(application);
    }


    private void initALog(Application application) {
//        ALog.configALog(application, "9rqKSi8gkL");
        ALog.setLevel(ALog.LEVEL_DEBUG);

        com.aliyun.alink.linksdk.tools.ALog.setLevel(com.aliyun.alink.linksdk.tools.ALog.LEVEL_DEBUG);
    }

    private void _initBaseSdk(AApplication application) {
        SDKManager.Result result = null;

        // API网关
        {
            SDKConfigure configure = new SDKConfigure("API-Client", "0.0.1", APIGatewaySDKDelegate.class.getName());

            result = new SDKManager.Result();
            result.sdkName = "APIGateway";
            result.sdkVer = "*";
            result.bInitialized = true;
            result.resultCode = new APIGatewaySDKDelegate().init(application, configure,AConfigure.getInstance().getConfig());
            SDKManager.InitResultHolder.updateResult(APIGatewaySDKDelegate.class.getName(), result);
        }

        // 帐号初始化
        {
            SDKConfigure configure = new SDKConfigure("OpenAccount", "0.0.1", OpenAccountSDKDelegate.class.getName());

            result = new SDKManager.Result();
            result.sdkName = "OpenAccount";
            result.sdkVer = "*";
            result.bInitialized = true;
            result.resultCode = new OpenAccountSDKDelegate().init(application, configure, AConfigure.getInstance().getConfig());
            SDKManager.InitResultHolder.updateResult(OpenAccountSDKDelegate.class.getName(), result);
        }
    }

    private void _initOtherSdk(AApplication application) {
        SDKManager.prepareForInitSdk(application);
        SDKManager.init_outOfUiThread(application);
    }


}
