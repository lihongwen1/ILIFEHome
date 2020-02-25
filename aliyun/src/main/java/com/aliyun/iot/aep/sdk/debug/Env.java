package com.aliyun.iot.aep.sdk.debug;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.aliyun.iot.aep.sdk.framework.AApplication;
import com.aliyun.iot.aep.sdk.framework.utils.SpUtil;

import java.io.Serializable;

public class Env implements Serializable {

    private boolean isSwitched;

    private String productEnv;
    private String apiEnv;
    private String boneEnv;

    private static final String KEY = "env_my_debug_key";

    private static class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static final Env INSTANCE = new Env();
    }

    private Env() {
        getEnv();
    }
    public static Env getInstance() {
        return Env.SingletonHolder.INSTANCE;
    }


    public void storeEnv() {
        SpUtil.putObject(AApplication.getInstance(), KEY, this);
    }

    private Env getEnv() {
        Env env = SpUtil.getObject(AApplication.getInstance(), KEY);
        if (env != null) {
            if (!TextUtils.isEmpty(env.apiEnv)) {
                setApiEnv(env.apiEnv);
            }
            if (!TextUtils.isEmpty(env.boneEnv)) {
                setBoneEnv(env.boneEnv);
            }
            if (!TextUtils.isEmpty(env.productEnv)) {
                setProductEnv(env.productEnv);
            }
            setSwitched(env.isSwitched);
        }
        return this;
    }

    public String getProductEnv() {
        return productEnv;
    }

    public void setProductEnv(String productEnv) {
        this.productEnv = productEnv;
    }

    public String getApiEnv() {
        return apiEnv;
    }

    public void setApiEnv(String apiEnv) {
        this.apiEnv = apiEnv;
    }

    public String getBoneEnv() {
        return boneEnv;
    }

    public void setBoneEnv(String boneEnv) {
        this.boneEnv = boneEnv;
    }


    public boolean isSwitched() {
        return isSwitched;
    }

    public void setSwitched(boolean switched) {
        isSwitched = switched;
    }

    @Override
    public String toString() {
        return "Env{" +
                "productEnv='" + productEnv + '\'' +
                ", apiEnv='" + apiEnv + '\'' +
                ", boneEnv='" + boneEnv + '\'' +
                ", isSwitched='" + isSwitched + '\'' +
                '}';
    }
}
