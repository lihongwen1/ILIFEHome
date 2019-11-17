package com.aliyun.iot.aep.sdk.delegate;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.openaccount.ConfigManager;
import com.alibaba.sdk.android.openaccount.Environment;
import com.alibaba.sdk.android.openaccount.OpenAccountSDK;
import com.alibaba.sdk.android.openaccount.OpenAccountService;
import com.alibaba.sdk.android.openaccount.OpenAccountSessionService;
import com.alibaba.sdk.android.openaccount.callback.InitResultCallback;
import com.alibaba.sdk.android.openaccount.model.OpenAccountSession;
import com.alibaba.sdk.android.openaccount.session.SessionListener;
import com.alibaba.sdk.android.openaccount.session.SessionManagerService;
import com.alibaba.sdk.android.openaccount.ui.OpenAccountUIConfigs;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.login.ILoginCallback;
import com.aliyun.iot.aep.sdk.login.ILogoutCallback;
import com.aliyun.iot.aep.sdk.login.IRefreshSessionCallback;
import com.aliyun.iot.aep.sdk.login.data.SessionInfo;
import com.aliyun.iot.aep.sdk.login.data.UserInfo;
import com.aliyun.iot.aep.sdk.threadpool.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by feijie.xfj on 17/10/30.
 * 默认实现一个OALoginAdapter
 */


public class OALoginAdapter extends com.aliyun.iot.aep.sdk.login.oa.OALoginAdapter {

    private final String TAG = "OALoginAdapter";


    private Context context;

    //当业务方调用 refreshSession时，刷新成功的回调，只保证一次有效
    private volatile List<IRefreshSessionCallback> mRefreshCallbacks = new ArrayList<>();

    private volatile boolean isRefreshing = false;

    private SessionListener defaultOASessionListener;

    private SessionInfo sessionInfo = new SessionInfo();

    /* --         Method         --*/


    public OALoginAdapter(Context context) {
        this(context, false);

    }


    public OALoginAdapter(Context context, boolean debug) {
        super(context);
        this.context = context;
        setIsDebuggable(debug);

    }


    public void setDefaultOAHost(String host) {
        if (!TextUtils.isEmpty(host)) {
            ConfigManager.getInstance().setApiGatewayHost(host);
        }
    }

    @Override
    public void init(String env,String imagePostfix, final OALoginAdapter.OALoginAdapterInitResultCallback initResultCallback) {
        log("init() OALoginAdapter , env is:" + env+" imagePostfix:"+imagePostfix);
        /**
         * 设置当前的环境信息
         * Environment.TEST 日常
         * Environment.PRE 预发
         * Environment.ONLINE 线上
         * */
        if ("TEST".equalsIgnoreCase(env)) {
            ConfigManager.getInstance().setEnvironment(Environment.TEST);
        } else if ("PRE".equalsIgnoreCase(env)) {
            ConfigManager.getInstance().setEnvironment(Environment.PRE);
        } else {
            ConfigManager.getInstance().setEnvironment(Environment.ONLINE);
        }
        if (!TextUtils.isEmpty(imagePostfix)) {
            ConfigManager.getInstance().setSecGuardImagePostfix(imagePostfix);
        } else {
            ConfigManager.getInstance().setSecGuardImagePostfix(EnvConfigure.AUTH_CODE);
        }

        ConfigManager.getInstance().setUseSingleImage(true);
        ConfigManager.getInstance().setAPIGateway(true);
        OpenAccountUIConfigs.showToolbar = true;
        OpenAccountUIConfigs.MobileRegisterFlow.supportForeignMobileNumbers = true;

        OpenAccountSDK.asyncInit(context, new InitResultCallback() {
            @Override
            public void onSuccess() {
                log("OpenAccountSDK init success");
                //增加默认OA Session 监听,当有Session变化时，必须通过此回调返回给调用者
                defaultOASessionListener = new OASessionListener();
                OpenAccountService
                        openAccountService = OpenAccountSDK.getService(OpenAccountService.class);
                openAccountService.addSessionListener(defaultOASessionListener);
                updateSession();
                if (initResultCallback!=null){
                    initResultCallback.onInitSuccess();
                }
            }

            @Override
            public void onFailure(int i, String s) {
                log("OpenAccountSDK init failed:" + s);
                if (initResultCallback!=null){
                    initResultCallback.onInitFailed(i,s);
                }
            }

        });

    }



    /*
     * 实际上调用refresh ,底层会先判断是否失效然后再决定要不要刷新
     * */
    @Override
    public void refreshSession(final boolean force, IRefreshSessionCallback sessionListener) {
        if (sessionListener != null) {
            mRefreshCallbacks.add(sessionListener);

        }
        if (isRefreshing) {
            return;
        }
        ThreadPool.DefaultThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                log("refreshSession() is force:" + force);
                OpenAccountSessionService
                        openAccountSessionService = OpenAccountSDK.getService(OpenAccountSessionService.class);
                openAccountSessionService.refreshSession(force);

            }

        });

    }


    @Override
    public void login(ILoginCallback callback, Map<String, String> map) {
        super.login(callback, map);
    }

    @Override
    public void logout(ILogoutCallback callback) {
        super.logout(callback);
    }

    @Override
    public boolean isLogin() {
        return super.isLogin();
    }


    @Override
    public UserInfo getUserData() {
        return super.getUserData();
    }

    @Override
    public Object getSessionData() {
        return sessionInfo;

    }

    @Override
    public String getSessionId() {
        return sessionInfo.sessionId;

    }


    private void updateSession() {
        SessionManagerService
                sessionManagerService = OpenAccountSDK.getService(SessionManagerService.class);
        if (sessionManagerService == null) {
            return;

        }
        if (sessionInfo == null) {
            sessionInfo = new SessionInfo();

        }
        sessionInfo.sessionCreateTime = sessionManagerService.getSessionCreationTime();
        sessionInfo.sessionId = sessionManagerService.getSessionId();
        sessionInfo.sessionExpire = sessionManagerService.getSessionExpiredIn();
        log("updateSession() sessionInfo:" + sessionInfo.toString());

    }


    private class OASessionListener implements SessionListener {

        @Override
        public void onStateChanged(OpenAccountSession openAccountSession) {
            log("onStateChanged() refreshCacheList size:" + mRefreshCallbacks.size());
            updateSession();
            if (!mRefreshCallbacks.isEmpty()) {
                dealCacheRefreshListeners();

            }
            isRefreshing = false;

        }

    }

    /**
     * 处理缓存的callback
     */


    private synchronized void dealCacheRefreshListeners() {
        log("dealCacheRefreshListeners() Deal cache listener size:" + mRefreshCallbacks.size());
        for (IRefreshSessionCallback callback : mRefreshCallbacks) {
            if (callback != null) {
                callback.onRefreshSuccess();

            }

        }
        mRefreshCallbacks.clear();

    }


    private void log(String str) {
        Log.i(TAG, str);

    }
}
