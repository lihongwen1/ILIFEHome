package com.aliyun.iot.aep.sdk.delegate;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.wireless.security.jaq.JAQException;
import com.alibaba.wireless.security.jaq.SecurityInit;

import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientImpl;
import com.aliyun.iot.aep.sdk.apiclient.adapter.APIGatewayHttpAdapterImpl;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTResponse;
import com.aliyun.iot.aep.sdk.apiclient.emuns.Env;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequest;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequestWrapper;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.framework.sdk.SDKConfigure;
import com.aliyun.iot.aep.sdk.framework.sdk.SimpleSDKDelegateImp;
import com.aliyun.iot.aep.sdk.log.ALog;

import java.util.Map;

/**
 * Created by wuwang on 2017/10/30.
 */
public final class APIGatewaySDKDelegate extends SimpleSDKDelegateImp {

    public final static String ENV_KEY_API_CLIENT_API_ENV = "KEY_API_CLIENT_API_ENV";
    public final static String ENV_KEY_API_CLIENT_DEFAULT_HOST = "KEY_API_CLIENT_DEFAULT_HOST";

    static final private String TAG = "APIGatewaySDKDelegate";

    static final private String HOST_ONLINE = "api.link.aliyun.com";
    static final private String HOST_TEST = "api-performance.aliplus.com";

    static Env apiEnv;
    static String host;

    /* API: ISDKDelegate */

    @Override
    public int init(Application app, SDKConfigure sdkConfigure, Map<String, String> args) {
        int ret = 0;

        // 初始化无线保镖
        try {
            ret = SecurityInit.Initialize(app);
        } catch (JAQException ex) {
            ALog.e(TAG, "security-sdk-initialize-failed", ex);

            ret = null != ex ? ex.getErrorCode() : -9999;
        } catch (Exception ex) {
            ALog.e(TAG, "security-sdk-initialize-failed", ex);

            ret = -1;
        }

        // get args from args
        Env apiEnv = Env.RELEASE;
        String env = args.get(ENV_KEY_API_CLIENT_API_ENV);


        if ("PRE".equalsIgnoreCase(env)) {
            apiEnv = Env.PRE;
        } else if ("TEST".equalsIgnoreCase(env)) {
            apiEnv = Env.TEST;

        } else {
            env = "RELEASE";
            apiEnv = Env.RELEASE;
        }

        String host = args.get(ENV_KEY_API_CLIENT_DEFAULT_HOST);
        if (TextUtils.isEmpty(host)) {
            if ("TEST".equals(env)) {
                host = HOST_TEST;
            } else {
                host = HOST_ONLINE;
            }
        }

        String language = args.get(EnvConfigure.KEY_LANGUAGE);
        if (TextUtils.isEmpty(language)) {
            language = "zh-CN";
        }

        // 初始化 IoTAPIClient
        IoTAPIClientImpl.InitializeConfig config = new IoTAPIClientImpl.InitializeConfig();
        config.host = host;
        config.apiEnv = apiEnv;
        config.authCode = EnvConfigure.AUTH_CODE;

        IoTAPIClientImpl impl = IoTAPIClientImpl.getInstance();
        impl.init(app, config);
        impl.setLanguage(language);

        // 日志处理
        impl.registerTracker(new Tracker());

        // 添加环境变量
        String appKey = APIGatewayHttpAdapterImpl.getAppKey(app,  EnvConfigure.AUTH_CODE);
        Log.d("APIGatewaySDKDelegate","项目的appkey:     "+appKey);
        args.put(EnvConfigure.KEY_APPKEY, appKey);
        args.put(ENV_KEY_API_CLIENT_DEFAULT_HOST, host);
        args.put(ENV_KEY_API_CLIENT_API_ENV, env);

        // 为了打日志
        APIGatewaySDKDelegate.apiEnv = Env.valueOf(env);
        APIGatewaySDKDelegate.host = host;

        return ret;
    }

    private static class Tracker implements com.aliyun.iot.aep.sdk.apiclient.tracker.Tracker {
        final String TAG = "APIGatewaySDKDelegate$Tracker";

        @Override
        public void onSend(IoTRequest request) {
            ALog.i(TAG, "onSend:\r\n" + toString(request));
        }

        @Override
        public void onRealSend(IoTRequestWrapper ioTRequest) {
            ALog.d(TAG, "onRealSend:\r\n" + toString(ioTRequest));
        }

        @Override
        public void onRawFailure(IoTRequestWrapper ioTRequest, Exception e) {
            ALog.d(TAG, "onRawFailure:\r\n" + toString(ioTRequest) + "ERROR-MESSAGE:" + e.getMessage());
            e.printStackTrace();
        }

        @Override
        public void onFailure(IoTRequest request, Exception e) {
            ALog.i(TAG, "onFailure:\r\n" + toString(request) + "ERROR-MESSAGE:" + e.getMessage());
        }

        @Override
        public void onRawResponse(IoTRequestWrapper request, IoTResponse response) {
            ALog.d(TAG, "onRawResponse:\r\n" + toString(request) + toString(response));
        }

        @Override
        public void onResponse(IoTRequest request, IoTResponse response) {
            ALog.i(TAG, "onResponse:\r\n" + toString(request) + toString(response));
        }

        private static String toString(IoTRequest request) {
            return new StringBuilder("Request:").append("\r\n")
                    .append("url:").append(request.getScheme()).append("://").append(null == request.getHost() ? "" : request.getHost()).append(request.getPath()).append("\r\n")
                    .append("apiVersion:").append(request.getAPIVersion()).append("\r\n")
                    .append("params:").append(null == request.getParams() ? "" : JSON.toJSONString(request.getParams())).append("\r\n").toString();
        }

        private static String toString(IoTRequestWrapper wrapper) {
            IoTRequest request = wrapper.request;

            return new StringBuilder("Request:").append("\r\n")
                    .append("id:").append(wrapper.payload.getId()).append("\r\n")
                    .append("apiEnv:").append(apiEnv).append("\r\n")
                    .append("url:").append(request.getScheme()).append("://").append(TextUtils.isEmpty(wrapper.request.getHost()) ? host : wrapper.request.getHost()).append(request.getPath()).append("\r\n")
                    .append("apiVersion:").append(request.getAPIVersion()).append("\r\n")
                    .append("params:").append(null == request.getParams() ? "" : JSON.toJSONString(request.getParams())).append("\r\n")
                    .append("payload:").append(JSON.toJSONString(wrapper.payload)).append("\r\n").toString();
        }

        private static String toString(IoTResponse response) {
            return new StringBuilder("Response:").append("\r\n")
                    .append("id:").append(response.getId()).append("\r\n")
                    .append("code:").append(response.getCode()).append("\r\n")
                    .append("message:").append(response.getMessage()).append("\r\n")
                    .append("localizedMsg:").append(response.getLocalizedMsg()).append("\r\n")
                    .append("data:").append(null == response.getData() ? "" : response.getData().toString()).append("\r\n").toString();
        }
    }
}