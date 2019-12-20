package com.aliyun.iot.aep.sdk._interface;

public interface OnAliBindDeviceResponse<T> {
    void onSuccess(T result);

    void onFailed(int code, String message);

    void onProgress(int progress);

    void manualConnectHomeWifi();
}
