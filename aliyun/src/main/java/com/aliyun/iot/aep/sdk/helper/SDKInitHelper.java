package com.aliyun.iot.aep.sdk.helper;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.sdk.android.openaccount.ui.OpenAccountUIConfigs;
import com.aliyun.alink.linksdk.alcs.coap.AlcsCoAP;
import com.aliyun.iot.aep.sdk.IoTSmart;
import com.aliyun.iot.aep.sdk.debug.Env;
import com.aliyun.iot.aep.sdk.framework.AApplication;
import com.aliyun.iot.aep.sdk.framework.config.GlobalConfig;
import com.aliyun.iot.aep.sdk.log.ALog;
import com.aliyun.iot.aep.sdk.login.LoginBusiness;
import com.aliyun.iot.aep.sdk.login.oa.OALoginAdapter;
import com.aliyun.iot.aep.sdk.page.AliEmailLoginActivity;
import com.aliyun.iot.aep.sdk.page.AliLoginActivity;
import com.aliyun.iot.aep.sdk.page.OAMobileCountrySelectorActivity;

import static com.aliyun.iot.aep.sdk.IoTSmart.REGION_ALL;
import static com.aliyun.iot.aep.sdk.IoTSmart.REGION_CHINA_ONLY;

public class SDKInitHelper {
    private static final String TAG = "SDKInitHelper";

    /**
     * 初始化阿里云SDK
     *
     * @param app
     * @param buildCountry CHINA --REGION_CHINA_ONLY 其它 REGION_ALL
     */
    public static void init(AApplication app, String buildCountry) {
        if (app == null) {
            return;
        }
        preInit(app);
        onInit(app, buildCountry);
        //onInitDefault(app);
        postInit(app, buildCountry);
    }

    /**
     * 初始化之前准备工作/google/face book
     *
     * @param app
     */
    private static void preInit(AApplication app) {
        //要在OA初始化前调用
//        ConfigManager.getInstance().setGoogleClientId(app.getString(R.string.server_client_id));
//
//        String appId = app.getString(R.string.facebook_app_id);
//        FacebookSdk.setApplicationId(appId);
//        ConfigManager.getInstance().setFacebookId(appId);
    }

    /**
     * 默认初始化
     *
     * @param app
     */
    private static void onInitDefault(AApplication app) {
        GlobalConfig.getInstance().setApiEnv(GlobalConfig.API_ENV_PRE);
        GlobalConfig.getInstance().setBoneEnv(GlobalConfig.BONE_ENV_TEST);

        IoTSmart.init(app);
    }

    /**
     * 带参数初始化
     *
     * @param app
     */
    private static void onInit(AApplication app, String buildCountry) {
        // 默认的初始化参数
        IoTSmart.InitConfig initConfig = new IoTSmart.InitConfig()
                // REGION_ALL: 支持连接中国大陆和海外多个接入点，REGION_CHINA_ONLY:直连中国大陆接入点，只在中国大陆出货选这个
                .setRegionType(buildCountry.equals("CHINA") ? REGION_CHINA_ONLY : REGION_ALL)
                // 对应控制台上的测试版（PRODUCT_ENV_DEV）和正式版（PRODUCT_ENV_PROD）(默认)
                .setProductEnv(IoTSmart.PRODUCT_ENV_PROD)
                // 是否打开日志
                .setDebug(true);

        //TODO 定制三方通道离线推送，目前支持华为、小米和FCM,参考阿里云demo
        GlobalConfig.getInstance().setApiEnv(GlobalConfig.API_ENV_ONLINE);
        GlobalConfig.getInstance().setBoneEnv(GlobalConfig.BONE_ENV_TEST);
        ALog.d(TAG, "initConfig1:" + JSON.toJSONString(initConfig));
        // 切换国内，国外环境，测试版、正式版环境后的初始化参数
        Env env = Env.getInstance();
        ALog.d(TAG, "env:" + env.toString());
        if (env.isSwitched()) {
            if (!TextUtils.isEmpty(env.getApiEnv())) {
                GlobalConfig.getInstance().setApiEnv(env.getApiEnv());
            }
            if (!TextUtils.isEmpty(env.getBoneEnv())) {
                GlobalConfig.getInstance().setBoneEnv(env.getBoneEnv());
            }
            if (!TextUtils.isEmpty(env.getProductEnv())) {
                initConfig.setProductEnv(env.getProductEnv());
            }

            ALog.d(TAG, "initConfig2:" + JSON.toJSONString(initConfig));
        }
        // 初始化
        IoTSmart.init(app, initConfig);

        new AlcsCoAP().setLogLevel(ALog.LEVEL_ERROR);

    }

    /**
     * 初始化后的定制参数
     *
     * @param app application
     */
    private static void postInit(@SuppressWarnings("unused") AApplication app, String buildCountry) {

        OALoginAdapter adapter = (OALoginAdapter) LoginBusiness.getLoginAdapter();
        boolean isSupportForeignMobile = buildCountry.equals("CHINA");
        if (adapter != null) {
            if (isSupportForeignMobile) {
                adapter.setDefaultLoginClass(AliLoginActivity.class);
            } else {
                adapter.setDefaultLoginClass(AliEmailLoginActivity.class);
            }
        }
        OpenAccountUIConfigs.AccountPasswordLoginFlow.supportForeignMobileNumbers = isSupportForeignMobile;
        OpenAccountUIConfigs.AccountPasswordLoginFlow.mobileCountrySelectorActvityClazz = OAMobileCountrySelectorActivity.class;

        OpenAccountUIConfigs.ChangeMobileFlow.supportForeignMobileNumbers = isSupportForeignMobile;
        OpenAccountUIConfigs.ChangeMobileFlow.mobileCountrySelectorActvityClazz = OAMobileCountrySelectorActivity.class;

        OpenAccountUIConfigs.MobileRegisterFlow.supportForeignMobileNumbers = isSupportForeignMobile;
        OpenAccountUIConfigs.MobileRegisterFlow.mobileCountrySelectorActvityClazz = OAMobileCountrySelectorActivity.class;

        OpenAccountUIConfigs.MobileResetPasswordLoginFlow.supportForeignMobileNumbers = isSupportForeignMobile;
        OpenAccountUIConfigs.MobileResetPasswordLoginFlow.mobileCountrySelectorActvityClazz = OAMobileCountrySelectorActivity.class;

        OpenAccountUIConfigs.OneStepMobileRegisterFlow.supportForeignMobileNumbers = isSupportForeignMobile;
        OpenAccountUIConfigs.OneStepMobileRegisterFlow.mobileCountrySelectorActvityClazz = OAMobileCountrySelectorActivity.class;
    }
}
