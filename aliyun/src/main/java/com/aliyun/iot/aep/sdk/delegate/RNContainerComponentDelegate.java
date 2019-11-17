package com.aliyun.iot.aep.sdk.delegate;

import android.app.Application;
import android.text.TextUtils;

import com.aliyun.alink.page.rn.InitializationHelper;
import com.aliyun.alink.sdk.bone.plugins.config.BoneConfig;
import com.aliyun.iot.aep.routerexternal.RouterExternal;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.framework.sdk.SDKConfigure;
import com.aliyun.iot.aep.sdk.framework.sdk.SimpleSDKDelegateImp;

import java.util.Map;

/**
 * RN容器
 */
public class RNContainerComponentDelegate extends SimpleSDKDelegateImp {

    static final public String KEY_RN_CONTAINER_PLUGIN_ENV = "KEY_RN_CONTAINER_PLUGIN_ENV";

    private final static String TAG = "RNContainerComponentDelegate";

    @Override
    public int init(final Application application, SDKConfigure sdkConfigure, Map<String, String> map) {
        // plugin env
        String pluginEnv = "test";
        pluginEnv = map.get(KEY_RN_CONTAINER_PLUGIN_ENV);
        if (TextUtils.isEmpty(pluginEnv)) {
            pluginEnv = "test";
        }
        map.put(KEY_RN_CONTAINER_PLUGIN_ENV, pluginEnv);

        // server env
        String serverEnv = map.get(APIGatewaySDKDelegate.ENV_KEY_API_CLIENT_API_ENV);
        if (TextUtils.isEmpty(serverEnv)) {
            serverEnv = "production"; // default is "production"
        } else if ("release".equalsIgnoreCase(serverEnv)) {
            serverEnv = "production";
        } else if ("pre".equalsIgnoreCase(serverEnv)) {
            serverEnv = "test";
        } else if ("test".equalsIgnoreCase(serverEnv)) {
            serverEnv = "development";
        }

        // language
        String language = map.get(EnvConfigure.KEY_LANGUAGE);
        if (TextUtils.isEmpty(language)) {
            language = "zh-CN";
        }

        // 初始化 BoneMobile 容器
        InitializationHelper.initialize(application, pluginEnv, serverEnv, language);
        // 添加基于 Fresco 的图片组件支持

        // 配置 region，
        // 国内环境设置为 china
        // 海外环境设置为 singapore
        BoneConfig.set("region", "china");
        RouterExternal.getInstance().init(application, pluginEnv);
        return 0;
    }

}
