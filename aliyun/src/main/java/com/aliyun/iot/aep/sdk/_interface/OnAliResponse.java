package com.aliyun.iot.aep.sdk._interface;

public interface OnAliResponse<T> {
    void onSuccess(T result);

    void onFailed(int code, String message);
}
